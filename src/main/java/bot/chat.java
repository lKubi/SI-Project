// Internal action code for project prueba.mas2j

package bot;

//import jason.*;
import jason.asSemantics.*;
import jason.asSyntax.*;
//import jason.util.asl2html;

//import java.io.*;
//import java.io.File;

//import java.util.*;
//import java.util.logging.Logger;

//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;

//import org.w3c.dom.Document;

//import org.alicebot.ab.Bot;
//import org.alicebot.ab.Chat;
//import org.alicebot.ab.History;
//import org.alicebot.ab.MagicBooleans;
//import org.alicebot.ab.MagicStrings;
//import org.alicebot.ab.utils.IOUtils;

import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionRequest;

//import okhttp.*;

//import retrofit2.*;

public class chat extends DefaultInternalAction {

	// Chat trace mode: Default => false
	//private static final boolean TRACE_MODE = false; 
	// AIML Bot name
	static String botName = "bot";
	//private asl2html agTransformer = new asl2html("/xml/agInspection-nd.xsl");
	//private Document agState;
	//private String sMind;

    @Override
    public Object execute(TransitionSystem ts, 
	                      Unifier un, 
						  Term[] args) throws Exception {

		// get the message
        String request = ((StringTerm)args[0]).getString();	
		String response = "No sé si tengo algo que decir";
		String token = System.getenv("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");
        OpenAiService service = new OpenAiService(token);


		try {

			/*
			Creating a virtual personality for an Agent using AIML
			
			
			// Firstly get the right path to the AIML files
			String resourcesPath = getResourcesPath();
			//System.out.println(resourcesPath);
			
			MagicBooleans.trace_mode = TRACE_MODE;
			Bot bot = new Bot(botName, resourcesPath);
			Chat chatSession = new Chat(bot);
			bot.brain.nodeStats();
			String textLine = "";

			// Get the bot response to the request
			response = chatSession.multisentenceRespond(request);
			
			// Normalize < and > for labels
			while (response.contains("&lt;")) response = response.replace("&lt;", "<");
			while (response.contains("&gt;")) response = response.replace("&gt;", ">");
			
			//agState = ts.getAg().getAgState();
			//sMind = agTransformer.transform(agState);*/
			System.out.println("\nCreating completion...");
			CompletionRequest completionRequest = CompletionRequest.builder()
                .model("ada")
                //.prompt("Somebody once told me the world is gonna roll me")
				.prompt(request)
                .echo(true)
                .user("testing")
                .build();
				service.createCompletion(completionRequest).getChoices().forEach(System.out::println);
		
		} catch (Exception eLabel) {
			eLabel.printStackTrace();
		};
				
        //return true;
		StringTerm result = new StringTermImpl(response);
		return un.unifies(result, args[1]);
		//StringTerm agMind = new StringTermImpl(sMind);
		//return un.unifies(agMind, args[1]);
			
     }
	 
	 /* 
	private static String getResourcesPath() {
		File currDir = new File(".");
		String path = currDir.getAbsolutePath();
		path = path.substring(0, path.length() - 2);
		//System.out.println(path);
		//logger.info(path);
		String resourcesPath = path + File.separator + "src" + File.separator + "resources";
		return resourcesPath;
	}
	*/
	 
}

