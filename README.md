# SI-Project
Proyecto de Sistemas inteligentes
Primera Parte del Proyecto
A partir de esta semana se comienza la realización del proyecto de la materia. Este proyecto va a ser desarrollado en dos fases (incrementos) que serán abordadas semanalmente mediante una serie de iteraciones dirigidas por una serie de objetivos que deberían alcanzarse al finalizar cada semana de trabajo. Para facilitar la distribución de esfuerzos y el reparto de trabajo, cada semana se propondrán una serie de ejercicios que cubrirán dichos objetivos. Las soluciones a algunos de estos ejercicios serán comentadas al comienzo de las clases de la siguiente semana y se publicarán indicaciones de las mismas (al término de la siguiente semana) 

Tened en cuenta que con la entrega del proyecto se os pedirá una memoria que documente adecuadamente la solución entregada utilizando los modelos vistos en las semanas anteriores al presentar las distintas partes de creación, modificación y diseño de los proyectos en Jason. Por esto se recomienda que todas las semanas, junto con el código, documentéis las soluciones que construyáis para evitar colapsar en la semana de entrega; de hecho, una buena práctica sería pensar primero en el diseño de la solución, documentarla y  a continuación construirla/implementarla.

Objetivos de la Primera Fase del Proyecto
En esta primera parte hay que realizar una modificación al ejemplo de robot domestico que ya se ha presentado, para transformarlo en un proyecto de un agente asistencial doméstico. En esta primera versión tendremos solo dos agentes: el agente owner y un agente robot (enfermera). 

El agente owner tiene necesidad de tomar sus medicaciones pautadas (deberéis elegir al menos 5 medicamentos y la pauta de toma de cada uno de ellas) El agente owner puede moverse libremente por la casa y descansar en zonas adecuadas para ello (silla, sofá, cama, ...) 
La medicación puede ser proporcionada por el agente robot o por el propio agente owner. Para que el robot pueda servir los medicamentos al owner, este debe indicar la pauta de las medicaciones al robot nada más crearse. 
En caso de haber tomado la medicación, el owner debe indicárselo al robot.
Con el paso del tiempo, el owner modificará la pauta de sus medicaciones y añadirá y/o eliminará algunas de ellas. Las medicaciones deberán guardarse en sitios adecuados y accesibles tanto para el owner como para el robot.
La medicación estará disponible en cantidad siempre suficiente para poder ser administrada.
El agente robot debe poder moverse libremente por la casa sorteando los objetos que en ella se encuentra, de una habitación a otra, buscando al owner para entregarle la medicación cuando proceda.  
Si el robot recibe indicación de que alguna medicación se ha tomado, debe comprobar que ha sido así.
Cada semana se irán proponiendo objetivos concretos de mejora y cambio en el proyecto.

============================================================================

# Objetivos para la semana 1

El objetivo de esta semana es conseguir adecuar el entorno doméstico a vuestras necesidades: 

Modificar el desplazamiento de los agentes en el entorno, de forma que solo se desplace a izquierda, derecha, arriba o abajo una casilla cada vez, evitando los obstáculos que aparezcan
Añadir una o varias zonas de almacenamiento para medicamentos.
Conseguir que los agentes se posicionen al lado de la zona de almacenamiento y al lado uno de otro.
Visualizar la apertura de la zona de almacenamiento y el decremento de medicamentos.
