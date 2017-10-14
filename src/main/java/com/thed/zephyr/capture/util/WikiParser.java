package com.thed.zephyr.capture.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

@Component
public class WikiParser {
	ScriptEngineManager scriptEngineManager = new ScriptEngineManager(null);
    ScriptEngine engine = scriptEngineManager.getEngineByName("JavaScript");
    private static Logger log = LoggerFactory.getLogger("application");
	private ResourceLoader resourceLoader;

	public WikiParser(ResourceLoader resourceLoader)  {
		try {
			InputStream is = null;
			try {
				log.info("creating input stream to read js file");
				Resource resource = resourceLoader.getResource("classpath:/static/js/zephyr-wiki-parser.js");
				is = resource.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String s, functionScript = "";
				log.debug("start reading js file ");
				while ((s = br.readLine()) != null) {
					functionScript += s;
				}
				log.debug("Parser JS file is read");
				engine.eval(functionScript);
				log.debug("Eval is done ");

			} catch (Exception e) {
				log.error("Error in creating parser", e);
			} finally {
				if (is != null)
					is.close();
			}
		}catch (Exception e){

		}
    }
	public String parseWiki(String markup, String format) {
		String scripts = "" ;
		try{        
	        if(engine instanceof Invocable) { 
	        	// wiki to html
	        	if(format.equals("html")) {
	        		scripts = "var parser = new ZephyrWikiParser(); var wikiToContent = parser.wikiToHTML";
	        	} else if (format.equals("text")) { // wiki to text
	        		scripts = "var parser = new ZephyrWikiParser(); var wikiToContent = parser.wikiToText";
	        	}
	            
	            engine.eval(scripts);
	            Object output = ((Invocable)engine).invokeFunction("wikiToContent", markup);
	            return (String) output;
	        }
	    }
	    catch(Exception e){
            log.error("Error in parsing", e);
	    }
		return null;
	}
}