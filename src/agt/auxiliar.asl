/* ----- CONEXIONES ENTRE HABITACIONES ----- */
// Definimos las conexiones entre las diferentes habitaciones y sus puertas
// Esto permite que el robot pueda moverse de una habitación a otra a través de puertas específicas.
connect(kitchen, hall, doorKit1).
connect(kitchen, hallway, doorKit2).
connect(hall, kitchen, doorKit1).
connect(hallway, kitchen, doorKit2).
connect(bath1, hallway, doorBath1).
connect(bath2, bedroom1, doorBath2).
connect(hallway, bath1, doorBath1).
connect(bedroom1, bath2, doorBath2).
connect(bedroom1, hallway, doorBed1).
connect(hallway, bedroom1, doorBed1).
connect(bedroom2, hallway, doorBed2).
connect(hallway, bedroom2, doorBed2).
connect(bedroom3, hallway, doorBed3).
connect(hallway, bedroom3, doorBed3).
connect(hall, livingroom, doorSal1).
connect(livingroom, hall, doorSal1).
connect(hallway, livingroom, doorSal2).
connect(livingroom, hallway, doorSal2).

//Si queremos añadir mas creencias caduca, hay que añadirlas tambien en HouseModel, para poder verlas visualmente
caduca("Amoxicilina", 1, 00).
caduca("Omeprazol", 2, 00).
caduca("Ibuprofeno",3, 00).
caduca("Loratadina", 14, 00).
caduca("Paracetamol", 18, 00).


// Umbral para ir a cargar Por ejemplo, ir a cargar cuando baje de 360
low_energy_threshold(360). 

free[source(self)].

!check_energy. 
!check_schedule.

+!enfermera_cargando[source(enfermera)] <-
      +enfermera_cargando;
      .drop_intention(check_energy).

+!enfermera_cargado[source(enfermera)] <-
      -enfermera_cargando;
      !check_energy;
      .println("Enfermera: El enfermera ha terminado de cargar, reactivando bucles principales.").


+!traer_medicamento(DrugName)[source(enfermera)] <- // ¡Cambiado de + a +!
     -free[source(self)];
     .println("AUXILIAR: Recibido objetivo !traer_medicamento('", DrugName, "') de enfermera."); // Log para confirmar
     !at(auxiliar, medCab);
     open(medCab);
     obtener_medicamento(DrugName);
     close(medCab);
     !at(auxiliar, enfermera);
     transferir_medicamento_enfermera(DrugName);
     .send(enfermera, tell, medicamento_traido_auxiliar(DrugName));
     // No es necesario -traer_medicamento(DrugName) para objetivos de logro,
     // ya que se eliminan automáticamente al completarse o fallar el plan.
     +free[source(self)];
     !check_energy.

    

// Se activa si la energía actual (CE) es menor que el umbral (T)
+!check_energy : current_energy(CE) & low_energy_threshold(T) & CE < T & free[source(self)] & not enfermera_cargando <-
   .print("¡Energía baja (", CE, "/", T, ")! [Bucle Check] Necesito cargar.");
   !charge_battery.       

// Plan Bucle: Energía OK, o está OCUPADO, o YA está intentando cargar
+!check_energy : current_energy(CE) & low_energy_threshold(T) & ( CE >= T | not free[source(self)] ) <-
   //.print("[ENERGÍA OK o AGENTE OCUPADO: CE=", CE, "] Esperando...");
   .wait(100);  
   !check_energy.

-!check_energy : true <- 
   //.print("⚠️ Falló el objetivo por alguna razón desconocida, reintentando...");
   .wait(100);  
   !check_energy.

// Plan para lograr el objetivo de cargar la batería (Revisado)
+!charge_battery : free[source(self)] & .my_name(Ag) & not enfermera_cargando <-
    -free[source(self)]; // *** MARCARSE COMO OCUPADO ***
    .print("Iniciando secuencia de carga hacia el cargador (Agente ocupado)...");
    .print("Navegando hacia la zona del cargador...");
    !at(Ag, cargador);
        .print("Confirmado en/junto al cargador, iniciando carga...");
        start_charging;
        .send(enfermera, achieve, auxiliar_cargando); // Informa al auxiliar que cargue el robot
        .print("Carga iniciada, comenzando monitorización...");
        !wait_for_full_charge; // Este subobjetivo se encargará de la espera
        // El plan +!wait_for_full_charge debe añadir +free[source(self)] al final
.
// Plan para monitorizar la carga y detenerla cuando esté llena
+!wait_for_full_charge : current_energy(CE) & max_energy(ME) & CE >= ME & not enfermera_cargando <-
    .my_name(MySelf);
    .print(MySelf, ": ¡Batería llena (", CE, "/", ME, ")! Deteniendo carga.");
    stop_charging;
    .print(MySelf, ": CARGA: Carga completada y detenida. Agente libre.");
    .send(enfermera, achieve, auxiliar_cargado); // Informa al auxiliar que cargue el robot
    +free[source(self)];
    // Ensure !check_energy is re-activated if it was the one that led to charging
    // This depends on your agent's main loop structure. If !check_energy is a persistent goal, it might resume.
    // Or you might need to explicitly re-post it if it's not part of a larger recurring goal.
    // For now, let's assume the agent's main loops (!check_energy, !check_schedule) will naturally resume
    // once 'free[source(self)]' is true.
    .print(MySelf, ": Retomando bucles principales post-carga.");
    !check_schedule.

// Plan de depuración: Sigue esperando la carga completa
+!wait_for_full_charge : current_energy(CE) & max_energy(ME) & CE < ME <-
    .my_name(MySelf);
    .print(MySelf, ": [!wait_for_full_charge DEBUG] Esperando carga completa. Energía actual: ", CE, "/", ME, ". Libre: ", free[source(self)]);
    .wait(500); // Espera un poco antes de re-evaluar
    !wait_for_full_charge. // Re-envía el objetivo para seguir monitorizando

// Plan de depuración: Faltan creencias de energía o el objetivo es perseguido incorrectamente
+!wait_for_full_charge
    : .my_name(MySelf) & (not current_energy(_) | not max_energy(_))
<-
    .print(MySelf, ": [!wait_for_full_charge DEBUG] ERROR: Faltan creencias current_energy o max_energy. Reintentando en breve.");
    .wait(1000);
    !wait_for_full_charge.

// Plan de depuración: Fallback si !wait_for_full_charge se atasca por alguna otra razón
// Este es un plan de último recurso y debería activarse si los otros no lo hacen y el objetivo persiste.
// El 'true' como contexto lo hace muy general, así que úsalo con cuidado o añade más condiciones.
+!wait_for_full_charge : .my_name(MySelf) <-
    .print(MySelf, ": [!wait_for_full_charge DEBUG] Fallback genérico. Energía: ", current_energy(CE), "/", max_energy(ME), ". Libre: ", free[source(self)]);
    .wait(1500);
    !wait_for_full_charge.


/* ----- OBJETIVO: Ir a un lugar (!at) ----- */
+!at(Ag, P) : at(Ag, P) <-
    .wait(10).

+!at(Ag, P) : not at(Ag, P) & current_energy(CE) & CE > 0 <- 
      .println("[Auxiliar] Yendo a ", P); 
      !go(P);
      !at(Ag, P).

// Plan por si intenta !at con 0 energía (para que no se quede bloqueado sin hacer nada)
+!at(Ag, P) : not at(Ag, P) & current_energy(0) <- 
      .println("[Auxiliar] No puedo ir a ", P, ", ¡sin energía!").

+!at(Ag, P) : true <- 
   .println("[Auxiliar] Fallback de !at: intentando ir a ", P, " aunque no haya contexto completo.");
   !go(P).

+at(Ag, P) <- 
   .print("[Auxiliar] Confirmado: ", Ag, " está en ", P, ".").

/* ----- OBJETIVO: Navegar a un lugar (!go) ----- */
// Plan para ir dentro de la misma habitación
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomAg) & current_energy(CE) & CE > 0 <- 
   move_towards(P).

// Plan para ir a otra habitación, conectada directamente, estando ya en la puerta
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
          connect(RoomAg, RoomP, Door) & atDoor & current_energy(CE) & CE > 0 
   <- move_towards(P).

// Plan para ir a otra habitación, conectada directamente, SIN estar en la puerta
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
          connect(RoomAg, RoomP, Door) & not atDoor & current_energy(CE) & CE > 0
   <- move_towards(Door);
      !go(P). 

// Plan para ir a otra habitación, NO conectada directamente, SIN estar en la puerta intermedia
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
          not connect(RoomAg, RoomP, _) & connect(RoomAg, Room, DoorR) &
          connect(Room, RoomP, DoorP) & not atDoor & current_energy(CE) & CE > 0 
   <- move_towards(DoorR);
      !go(P). 

// Plan para ir a otra habitación, NO conectada directamente, ESTANDO en la puerta intermedia
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
          not connect(RoomAg, RoomP, _) & connect(RoomAg, Room, DoorR) &
          connect(Room, RoomP, DoorP) & atDoor & current_energy(CE) & CE > 0 
   <- move_towards(DoorP); 
      !go(P). 

// Plan Fallback (si los otros fallan pero las habitaciones son distintas)
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP & current_energy(CE) & CE > 0
   <- move_towards(P).

// Plan de fallo si no puede moverse por falta de energía
-!go(P)[error(E)] : current_energy(0) 
   <- .println("[Auxiliar] Fallo al intentar ir a ", P, " por falta de energía (0). Error: ", E).

// Plan de llegada 
-!go(P) <-
   .println("[Auxiliar] He llegado a ", P, "."). 

/* ----- OBJETIVO: Recibir y mostrar mensajes ----- */
+msg(M)[source(Ag)] : .my_name(Name) <-
    .print(Ag, " envió a ", Name, " el mensaje: '", M, "'").
   
 
// Plan Bucle: Comprobar horario/caducidades si está LIBRE y la Energía está OK
+!check_schedule
   : clock(H, M) 
<-
    //.print("DEBUG: [Bucle Check Schedule] Hora ", H, ":", M, ". Energía OK. Comprobando caducidades..."); 
   .findall(Drug, caduca(Drug, H, M), ExpiringDrugs); // Busca caducidades
   if (.length(ExpiringDrugs) > 0) {
       for ( .member(DrugToReponer, ExpiringDrugs) ) {
           .print("Iniciando proceso para caducidad de: ", DrugToReponer);
           .send(enfermera, tell, ha_caducado(DrugToReponer));
           .send(owner, tell, ha_caducado(DrugToReponer));
           .println("Enviando mensaje de caducidad a los otros agentes.");
           +esta_caducada(DrugToReponer, H, M);
       }
   } else {
       // .println("No hay caducidad programada."); 
   };
   .wait(100);
   !check_schedule. 

// Plan Bucle: Si está OCUPADO o la Energía está BAJA, simplemente esperar
+!check_schedule
   : ( not free[source(self)] ) <-
   // .print("DEBUG: [Bucle Check Schedule] Ocupado o Energía Baja, esperando..."); // Log opcional
   .wait(100); 
   !check_schedule.

// Plan Bucle: Si falta la hora 
+!check_schedule : not clock(_, _) <-
   .print("WARN: [Bucle Check Schedule] No se encontró la creencia clock(H,M). Esperando...");
   .wait(100); 
   !check_schedule.

+esta_caducada(NombreMedicina, HoraDetectada, MinutoDetectado)  
    : caduca(NombreMedicina, HoraDetectada, MinutoDetectado) <-
    .println("¡Detectada caducidad para: ", NombreMedicina, " a la hora ", HoraDetectada, MinutoDetectado, "!");
    .println("Procediendo a iniciar reposición...");
    -esta_caducada(NombreMedicina, HoraDetectada, MinutoDetectado);
    -caduca(NombreMedicina, HoraDetectada, MinutoDetectado);
    !reponer_medicamento(NombreMedicina, HoraDetectada, MinutoDetectado).

// En auxiliar.asl
+!reponer_medicamento(NombreMedicina, HoraVencimiento, MinutoVencimiento) <-
    .print("AUXILIAR: === DENTRO DE +!reponer_medicamento para ", NombreMedicina, " ===");
    .print("AUXILIAR: 'free' ANTES de -free: ", free[source(self)]);
    -free[source(self)]; 
    if (free[source(self)]) { // Esta condición debería ser falsa
        .print("AUXILIAR: ¡¡¡ERROR CRÍTICO!!! 'free[source(self)]' SIGUE PRESENTE después de '-free'.");
        +free[source(self)]; // Para asegurar que no se bloquee si hay error
        .fail_goal(reponer_medicamento(NombreMedicina, HoraVencimiento, MinutoVencimiento)); 
    } else {
        .print("AUXILIAR: BIEN. 'free[source(self)]' fue eliminada. El agente está OCUPADO.");
        .print("AUXILIAR: Ahora probando .intend(check_energy, II)...");
        if (.intend(check_energy, II)) { 
            .print("AUXILIAR: .intend(check_energy, II) ÉXITO. ID Intención: ", II);
            .print("AUXILIAR: Ahora probando .drop_intention(check_energy)...");
            if (.drop_intention(check_energy)) {
                .print("AUXILIAR: .drop_intention(check_energy) ÉXITO (intención eliminada).");
            } else {
                .print("AUXILIAR: .drop_intention(check_energy) DEVOLVIÓ FALSE (no se eliminó o no existía).");
            }
        } else {
            .print("AUXILIAR: .intend(check_energy, II) DEVOLVIÓ FALSE (o falló con ia_failed, revisa logs).");
        };
        .print("AUXILIAR: Pruebas de .intend/.drop completadas.");
        
        // Ahora reintroduce el resto del plan original:
        .my_name(Ag);
        .println("AUXILIAR: Procediendo a reponer medicamento (interno del plan): ", NombreMedicina); 
        !at(Ag, delivery);
        cargar_medicamento; 
        .println("Agente ", Ag, " recogiendo la medicacion de entrega.");
        .wait(1000);
        !at(Ag, medCab);
        open(medCab);
        reponer_medicamento(NombreMedicina);
        .send(enfermera, tell, stock_actualizado(NombreMedicina, 50));
        close(medCab);
        .wait(1000); 
        .println("Medicamento ", NombreMedicina, " repuesto.");
        .send(enfermera, tell, orden_eliminar_caducaciones);
        .send(owner, tell, orden_eliminar_caducaciones); 
        // +free[source(self)]; // Esta línea debería estar al final después de todas las acciones.
    };

    // Asegurar que el agente se libere al final del plan, sea cual sea el camino
    if (not bel(free[source(self)])) { 
        +free[source(self)];
        .print("AUXILIAR: +free[source(self)] AÑADIDO al final de +!reponer_medicamento.");
    } else {
        .print("AUXILIAR: Agente ya estaba 'free' al final de +!reponer_medicamento (inesperado si -free funcionó y el plan no se completó antes).");
    }
    .print("AUXILIAR: === FIN COMPLETO DE +!reponer_medicamento ===");
    !check_energy.
// --- Planes para ayudar al Robot sin batería ---

// Plan que se activa al recibir la señal del robot
// Asume que el Auxiliar está libre y tiene energía suficiente para ayudar
+robot_needs_energy(RobotName)[source(RobotName)] : free[source(self)] & current_energy(MyE) & MyE > 1 <- // Requiere > 1 para poder dar algo
    -free[source(self)]; // Se marca como ocupado
    .my_name(Me);
    .print("AUXILIAR (", Me, "): Recibido aviso de '", RobotName, "'. Mi energía: ", MyE, ". ¡Voy a ayudar!");

    // Ir hacia el robot. Asume que !at puede manejar "enfermera" como destino
    // gracias a la modificación sugerida en HouseEnv para move_towards.
    // Si no, necesitarías obtener las coords X,Y de 'enfermera' (si las percibes)
    // y usar !at(Me, loc(X,Y)) o similar.
    !at(Me, enfermera); // Objetivo: ir a la ubicación del robot

    .print("AUXILIAR (", Me, "): He llegado donde '", RobotName, "'. Transfiriendo energía...");

    // Ejecuta la acción del entorno para transferir energía
    transfer_energy_to_robot;
    -robot_needs_energy(enfermera)[source(enfermera)]; // Elimina la creencia de que el robot necesita energía

    .print("AUXILIAR (", Me, "): Transferencia de energía intentada.");

    // Ya no necesita la señal, la elimina (aunque es una creencia externa, puede ser útil quitarla localmente si no se usa más)
    // Opcional: -robot_needs_energy(RobotName)[source(RobotName)];
    +free[source(self)]; // Se marca como libre de nuevo

    // Reanuda sus bucles
    !check_energy;
    !check_schedule.


// Plan si el Auxiliar recibe la señal pero NO tiene energía suficiente para dar
+robot_needs_energy(RobotName)[source(RobotName)] : free[source(self)] & current_energy(MyE) & MyE <= 1 <-
    .my_name(Me);
    .print("AUXILIAR (", Me, "): Recibido aviso de '", RobotName, "', pero tengo muy poca energía (", MyE, ") para ayudar.");
    // Decide qué hacer: ¿ignorar? ¿intentar cargar él mismo primero?
    // Por ahora, solo lo ignora y sigue con sus tareas.
    // Opcional: podrías quitar la creencia para no volver a reaccionar inmediatamente
    // -robot_needs_energy(RobotName)[source(RobotName)];
    // Sigue sus bucles normales
    !check_energy;
    !check_schedule.


// Plan si el Auxiliar está ocupado cuando recibe la señal
+robot_needs_energy(RobotName)[source(RobotName)] : not free[source(self)] <-
    .my_name(Me);
    .print("AUXILIAR (", Me, "): Recibido aviso de '", RobotName, "', pero estoy ocupado. Lo intentaré más tarde.");
    // Espera un poco y confía en que la creencia persistirá o se reenviará
    .wait(5000).

   // Plan para asegurar que los bucles principales estén activos cuando el agente queda libre.
// Este plan se ejecuta CADA VEZ que se añade la creencia `free[source(self)]`.

+free[source(self)]
  // No se necesita un contexto específico más allá del evento en sí.
<-
  .my_name(MyAg); // Obtiene el nombre del agente actual
  .print(MyAg, ": ** Me he quedado LIBRE. Verificando bucles principales (!check_energy, !check_schedule). **");

  // Verificar e iniciar !check_energy si no está ya como una intención activa.
  // La función interna .intend(nombre_meta) comprueba si ya hay una intención para esa meta.
  if (not .intend(check_energy)) {
    .print(MyAg, ": El objetivo !check_energy no parece estar activo. Reiniciándolo AHORA.");
    !check_energy;
  } else {
    .print(MyAg, ": El objetivo !check_energy ya está activo o pendiente de ejecución.");
  };

  // Verificar e iniciar !check_schedule si no está ya como una intención activa.
  if (not .intend(check_schedule)) {
    .print(MyAg, ": El objetivo !check_schedule no parece estar activo. Reiniciándolo AHORA.");
    !check_schedule;
  } else {
    .print(MyAg, ": El objetivo !check_schedule ya está activo o pendiente de ejecución.");
  };
.