package com.moxtra.finance;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moxtra.bot.Chat;
import com.moxtra.bot.MoxtraBot;
import com.moxtra.bot.annotation.EventHandler;
import com.moxtra.bot.model.AccountLink;
import com.moxtra.bot.model.Button;
import com.moxtra.bot.model.ChatMessage;
import com.moxtra.bot.model.Comment;
import com.moxtra.bot.model.EventType;
import com.moxtra.finance.*;
import com.moxtra.finance.models.Symbol;


import org.json.*;

@Component
public class Stocks extends MoxtraBot {
	private static final Logger logger = LoggerFactory.getLogger(Stocks.class);
	
	// key - binder_id;  value - access_token
	public HashMap<String, String> moxtraAccessToken = new HashMap<String, String>();
	
	// key - binder_id + user_id;  value - Chat 
	public HashMap<String, Chat> pendingResponse = new HashMap<String, Chat>();
		
	// key - user_id;  value - Chat 
	public HashMap<String, Chat> pendingOAuth = new HashMap<String, Chat>();
	
	// key - user_id;  value - 3rd party access_token
	public HashMap<String, String> accountLinked = new HashMap<String, String>();
	
	//API Constants
	public String chartURL = "https://www.google.com/finance/getchart?q=";
	public String quoteURL = "http://finance.google.com/finance/info?q=";
	public String miniChartURL = "https://www.google.com/finance/chart?q=";
	public String currencyURL = "http://finance.google.com/finance/info?q=CURRENCY:";
	
	// ---------------- EventHandler methods -------------------
	
	//BOT_INSTALLED
	@EventHandler(event = EventType.BOT_INSTALLED)
	public void onBotInstalled(Chat chat) {
		String username = chat.getUsername();
		String binder_id = chat.getBinder_id();
		String access_token = chat.getAccess_token();
		
		// store binder based Moxtra access_token
		moxtraAccessToken.put(binder_id, access_token);
		
		StringBuilder richtext = new StringBuilder();
		richtext.append("[table][tr][td]Hello @" + username + "![/td][/tr]");
		richtext.append("[tr][td]Welcome to Moxtra Finbot!![/td][/tr]");
		richtext.append("[tr][td]To learn how to play with me, please type: @Finbot help[/td][/tr][/table]"); 
		
		chat.sendRequest(new Comment.Builder().richtext(richtext.toString()).build());
	}
	
	//BOT_UNINSTALLED
	@EventHandler(event = EventType.BOT_UNINSTALLED)
	public void onBotUninstalled(ChatMessage chatMessage) {
		String binder_id = chatMessage.getBinder_id();
		
		// remove Moxtra access_token for this binder
		moxtraAccessToken.remove(binder_id);
		
		logger.info("Bot uninstalled on {}", binder_id);
	}
	

	
	//SUMMARY: "@Finbot show (.*) summary"
	@EventHandler(patterns = {"@Finbot show (.*) summary"})
	public void getSummaryStock(Chat chat){
		Matcher matcher = chat.getMatcher();
		String stock = matcher.group(1);
		
		//chat.sendRequest(new Comment.Builder().text("Primatches:"+String.valueOf(chat.getPrimatches())).build());
		
		if (stock == null || stock == ""){
			chat.sendRequest(new Comment.Builder().text("Sorry, I didn't get the stock name.").build());
			return;
		}
		
		RestTemplate restTemplate = new RestTemplate();
		String jsonRet = "";
		StringBuilder richtext = new StringBuilder();
		
        try {
        	HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            logger.info(" > > > > > Getting stock summary for: "+stock+"! < < < < < ");
            
            //Calling Google Finance API to get the stock quote
        	ResponseEntity<String> response = restTemplate.exchange(quoteURL+stock, HttpMethod.GET, entity, String.class);
        	jsonRet = response.getBody().replace("//", "");
                        
            //convert JSON to Java symbs Object
            ObjectMapper mapper = new ObjectMapper();            
        	List<Symbol> symbs = mapper.readValue(jsonRet, new TypeReference<List<Symbol>>(){});
      
        	
        	String color = "";
        	for(Symbol symb : symbs){
        		if (symb.getC() == ""){
        			chat.sendRequest(new Comment.Builder().text("Sorry, I couldn't get the summary for: "+stock).build());
        			return;
        		}
        		
        		color = "#FF0000"; //red
        		if( Double.parseDouble(symb.getC()) > 0 )
        			color = "#008000"; //green
        		
        		richtext.append("[size=12]Sumary for "+symb.getE()+":[b]" + symb.getT() + "[/b][/size][table]");
        		richtext.append("[tr][td][size=28]USD " + symb.getL() + "[/size][/td][td][color="+color+"]" + symb.getC() + "[/color][/td][td][color="+color+"](" +symb.getCp() + "%)[/color][/td][td][img]"+ miniChartURL + symb.getT() +"&cht=s[/img][/td][/tr]");
        		richtext.append("[tr][td][size=10]" + symb.getLt() + "[/size][/td][/tr]");	
        		richtext.append("[/table]"); 
        	}
        } catch (RestClientException e) {
        	chat.sendRequest(new Comment.Builder().text("Sorry, I couldn't find the stock: "+stock+"/nPlease try: @Finbot show AAPL summary").build());
            logger.error(
					" > > > > > > getSummaryStock ERROR! " +
					"   Stock: " + stock + 
					" - Binder: " + chat.getBinder_id() +
					" - Message: "+e.getMessage()
					);
        }
        catch (JsonGenerationException ex) { 
        	ex.printStackTrace(); 
        	} 
        catch (JsonMappingException ex) { 
        	ex.printStackTrace(); 
        	}
		catch (IOException ex) {
	        ex.printStackTrace();
	    }
		
		chat.sendRequest(new Comment.Builder().richtext(richtext.toString()).build());
		
		
		
	}

	//SUMMARY: "@Finbot show (.*) quote"
	@EventHandler(patterns = {"@Finbot show (.*) quote"})
	public void getStockQuote(Chat chat){
		Matcher matcher = chat.getMatcher();
		String stock = matcher.group(1);
		
		if (stock == null || stock == ""){
			chat.sendRequest(new Comment.Builder().text("Sorry, I didn't get the stock name.").build());
			return;
		}
		
		RestTemplate restTemplate = new RestTemplate();
		String jsonRet = "";
		StringBuilder richtext = new StringBuilder();
		
        try {
        	HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            logger.info(" > > > > > Getting stock quote for: "+stock+"! < < < < < ");
            
            //Calling Google Finance API to get the stock quote
        	ResponseEntity<String> response = restTemplate.exchange(quoteURL+stock, HttpMethod.GET, entity, String.class);
        	jsonRet = response.getBody().replace("//", "");
                        
            //convert JSON to Java symbs Object
            ObjectMapper mapper = new ObjectMapper();            
        	List<Symbol> symbs = mapper.readValue(jsonRet, new TypeReference<List<Symbol>>(){});
      
        	
        	String color = "";
        	for(Symbol symb : symbs){
        		if (symb.getC() == ""){
        			chat.sendRequest(new Comment.Builder().text("Sorry, I couldn't get the quote for: "+stock).build());
        			return;
        		}
        		
        		color = "#FF0000"; //red
        		if( Double.parseDouble(symb.getC()) > 0 )
        			color = "#008000"; //green
        		
        		//quote
        		richtext.append("[size=12]Quote for "+symb.getE()+":[b]" + symb.getT() + "[/b][/size][table]");
        		richtext.append("[tr][td][size=28]USD " + symb.getL() + "[/size] [color="+color+"]" + symb.getC() + "[/color] [color="+color+"](" +symb.getCp() + "%)[/color][/td][/tr]");
        		
        		//after hours
        		if(symb.getEc_fix() != null && symb.getEc_fix() != ""){
        			color = "#FF0000"; //red
            		if( Double.parseDouble(symb.getEc_fix()) > 0 )
            			color = "#008000"; //green
            		richtext.append("[tr][td][size=12]After Hours: USD " + symb.getEl() + " [color="+color+"]" + symb.getEc() + " ("+symb.getEcp()+"%)[/color][/size][/td][/tr]");
        		}
        		
        		//last closing 
        		richtext.append("[tr][td][size=12]Previous close price: USD " + symb.getPcls_fix() + "[/size][/td][/tr]");
        		
        		//date last 
        		richtext.append("[tr][td][size=10]" + symb.getLt() + "[/size][/td][/tr]");

        		richtext.append("[/table]"); 
        		
        		
        		
        		
        		
        		//chart 60 days
        		richtext.append("[img=400x250]https://www.google.com/finance/getchart?q="+symb.getT()+"&p=60d[/img]");
        		
        		chat.sendRequest(new Comment.Builder().richtext(richtext.toString()).build());
        		
        		//chart(stock,chat,"&p=60d");
        	}
        } catch (RestClientException e) {
        	chat.sendRequest(new Comment.Builder().text("Sorry, I couldn't find the stock: "+stock+"/nPlease try: @Finbot show GOOG quote").build());
            logger.error(
					" > > > > > > getStockQuote ERROR! " +
					"   Stock: " + stock + 
					" - Binder: " + chat.getBinder_id() +
					" - Message: "+e.getMessage()
					);
        }
        catch (JsonGenerationException ex) { 
        	ex.printStackTrace(); 
        	} 
        catch (JsonMappingException ex) { 
        	ex.printStackTrace(); 
        	}
		catch (IOException ex) {
	        ex.printStackTrace();
	    }
	}
	
	//CURRENCY: @Finbot currency converter (.*) to (.*)
	@EventHandler(patterns = {"@Finbot currency converter (.*) to (.*)"})
	public void getCurrencyExchange(Chat chat){
		Matcher matcher = chat.getMatcher();
		String currencyFrom = matcher.group(1).toUpperCase();
		String currencyTO = matcher.group(2).toUpperCase();
		
		if (currencyFrom == null || currencyFrom == "" || currencyTO == null || currencyTO == ""){
			chat.sendRequest(new Comment.Builder().text("Sorry, I didn't get the currency symbols. Please, try: currency converter USD to EUR").build());
			return;
		}
		
		RestTemplate restTemplate = new RestTemplate();
		String ret = "";
		StringBuilder richtext = new StringBuilder();
		
        try {
        	HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            logger.info(" > > > > > Getting currency exchange from "+currencyFrom+" to "+currencyTO+" ! < < < < < ");
            
            //Calling Google Finance API to get the exchange
        	ResponseEntity<String> response = restTemplate.exchange(currencyURL+currencyFrom+currencyTO, HttpMethod.GET, entity, String.class);
            ret = response.getBody().replace("//", "");
                                  
            //convert JSON to Java symbs Object
            ObjectMapper mapper = new ObjectMapper();
        	List<Symbol> symbs = mapper.readValue(ret, new TypeReference<List<Symbol>>(){});
        	        	
        	for(Symbol symb : symbs){
        		if (symb.getL() == ""){
        			chat.sendRequest(new Comment.Builder().text("Sorry, I couldn't get the currencies for: "+currencyFrom+ " and "+currencyTO).build());
        			return;
        		}
        		
        		richtext.append("[size=14]Currency Converter:[/size][table]");
        		richtext.append("[tr][td] 1 " + symb.getT().substring(0, 2) + "[/td][/tr]");
        		richtext.append("[tr][td][size=28]" + symb.getL() + " " + symb.getT().substring(3) + "[/size][/td][/tr]");
        		richtext.append("[tr][td][size=10]" + symb.getLt() + "[/size][/td][/tr]");
        		richtext.append("[tr][td][img]https://www.google.com/finance/chart?q=CURRENCY:"+symb.getT()+"&tkr=1&p=2Y&chst=vkc&chs=300x160[/img][/td][/tr]");
        		richtext.append("[/table]"); 
        	}

        } catch (RestClientException e) {
        	chat.sendRequest(new Comment.Builder().text("Sorry, I couldn't find the currencies for: "+currencyFrom+ " and "+currencyTO+"/nPlease try: @Finbot currency converter USD to EUR").build());
            logger.error(
					" > > > > > > getCurrencyExchange ERROR! " +
					"   currencyFrom: " + currencyFrom +
					" - currencyTO: " + currencyTO +
					" - Binder: " + chat.getBinder_id() +
					" - Message: "+e.getMessage()
					);
        }
        catch (JsonGenerationException ex) { 
        	ex.printStackTrace(); 
        	} 
        catch (JsonMappingException ex) { 
        	ex.printStackTrace(); 
        	}
		catch (IOException ex) {
	        ex.printStackTrace();
	    }
        
        chat.sendRequest(new Comment.Builder().richtext(richtext.toString()).build());
				
	}
	
	//CHART: "@Finbot show (.*) (graph|chart)"
	@EventHandler(patterns = {"@Finbot show (.*) (graph|chart)"})
	public void getChart(Chat chat) throws IOException {
		Matcher matcher = chat.getMatcher();
		String stock = matcher.group(1).toUpperCase();
		
		if (stock == null || stock == ""){
			chat.sendRequest(new Comment.Builder().text("Sorry, I didn't get the stock name.").build());
			return;
		}
		
		
		Button postback_button1 = new Button("7 days");
		Button postback_button2 = new Button("30 days");
		Button postback_button3 = new Button("3 months");
		Button postback_button4 = new Button("1 year");
		postback_button1.setPayload(stock+"&p=7d");
		postback_button2.setPayload(stock+"&p=1M");
		postback_button3.setPayload(stock+"&p=3M");
		postback_button4.setPayload(stock+"&p=1Y");
		
		
		String message = "What's the period for your chart?";
		
		chat.sendRequest(new Comment.Builder().text(message).addButton(postback_button1)
				.addButton(postback_button2)
				.addButton(postback_button3)
				.addButton(postback_button4).build());
		

//		//Send message to the Bot
//		chat.sendRequest(new Comment.Builder().text("That is the stock graph for: "+ stock).build());
//		
//		//post the img to the binder
//		chart(stock, chat, "");
	}
	
	//private method to post the chart to the binder as an attached img
	private void chart(String stock, Chat chat, String period){
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();
		
		String tempDir = System.getProperty("java.io.tmpdir");
		File file = new File(tempDir + "/" + stock + "_stock_" + dateFormat.format(date) + ".png");
		
		try{	
			//Download the file
			URL url = new URL(chartURL+stock+period);
			BufferedImage img = ImageIO.read(url);
			
			ImageIO.write(img, "png", file);
			
			//Send message to the Bot
			chat.sendRequest(null, file, null);
			
			//Delete file from server
			file.delete();
		}
		catch(IOException e){
			logger.error(
					" > > > > > > chart ERROR! " +
					"   Stock: " + stock + 
					" - Filename: "+ file.getName() + 
					" - Binder: "+ chat.getBinder_id() + 
					" - Message: "+e.getMessage()
					);
		}
	}
	
	//get chart Button post back
	@EventHandler(event = EventType.POSTBACK)
	public void onPostback(Chat chat) {
		String payload = chat.getPostback().getPayload();
		String text = chat.getPostback().getText();
		
		chat.sendRequest(new Comment.Builder().text("Here is the chart for "+text).build());
		chart(payload,chat,"");
	}		
	
	//help @Finbot
	@EventHandler(patterns = {"@Finbot (help)?","help"})
	public void onHelp(Chat chat) {
		String username = chat.getUsername();
		
		if (chat.getPrimatches() == 0) {
			//chat.sendRequest(new Comment.Builder().text("Primatches:"+String.valueOf(chat.getPrimatches())).build());
			
			StringBuilder richtext = new StringBuilder();
			richtext.append("[table][tr][th]Hello " + username +  ", I'm your Moxtra Finance Bot![/th][/tr]");
			richtext.append("[tr][td]I'm still learning everyday, but for now you can try:[/td][/tr]");
			richtext.append("[tr][td]- @Finbot show [Stock name] summary[/td][/tr]");		
			richtext.append("[tr][td]- @Finbot show [Stock name] quote[/td][/tr]");
			richtext.append("[tr][td]- @Finbot show [Stock name] chart[/td][/tr]");
			richtext.append("[tr][td]- @Finbot currency converter [currency symbol] to [currency symbol][/td][/tr]");
			richtext.append("[/table]");  
			
			chat.sendRequest(new Comment.Builder().richtext(richtext.toString()).build());
		}
		
	}	
	
}
