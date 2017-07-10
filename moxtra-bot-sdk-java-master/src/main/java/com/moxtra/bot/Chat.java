package com.moxtra.bot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moxtra.bot.model.ChatMessage;
import com.moxtra.bot.model.Comment;
import com.moxtra.bot.model.EventAnnotate;
import com.moxtra.bot.model.EventBot;
import com.moxtra.bot.model.EventComment;
import com.moxtra.bot.model.EventFile;
import com.moxtra.bot.model.EventMeet;
import com.moxtra.bot.model.EventPage;
import com.moxtra.bot.model.EventPostback;
import com.moxtra.bot.model.EventTarget;
import com.moxtra.bot.model.EventTodo;
import com.moxtra.bot.model.EventType;
import com.moxtra.bot.model.EventUser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class Chat {
	private static final Logger logger = LoggerFactory.getLogger(Chat.class);
	private static final String URL_ENDPOINT = "https://apisandbox.moxtra.com/messages";
	private String user_id;
	private String username;
	private String binder_id;
	private String access_token;
	private int primatches = 0;
	private Matcher matcher;
	private EventType eventType = EventType.MESSAGE;
	private ChatMessage chatMessage;

	public Chat() {
	}

	public Chat(ChatMessage chatMessage) {
		setChatMessage(chatMessage);
	}

	public int getPrimatches() {
		return primatches;
	}

	public void setPrimatches(int primatches) {
		this.primatches = primatches;
	}

	public Matcher getMatcher() {
		return matcher;
	}

	public void setMatcher(Matcher matcher) {
		this.matcher = matcher;
	}

	@PostConstruct
	public boolean sendRequest(Comment comment) {

		// log sending message
		try {
			logger.info("Send: " + new ObjectMapper().writeValueAsString(comment));
		} catch (Exception e) {}

		RestTemplate restTemplate = new RestTemplate();

        try {

        	HttpHeaders headers = new HttpHeaders();
        	headers.set("Authorization", "Bearer " + this.access_token);

        	headers.setContentType(MediaType.APPLICATION_JSON);

        	HttpEntity<String> entity = new HttpEntity<String>(comment.toJSONString(), headers);

            restTemplate.postForEntity(URL_ENDPOINT, entity, String.class);

        } catch (JsonProcessingException e) {
            logger.error("Invalid message format!", e);
            return false;
        } catch (RestClientException e) {
            logger.error("Error posting message!", e);
            return false;
        }

        return true;
	}


	@PostConstruct
	public boolean sendRequest(Comment comment, File file, File audio) {

		if (file == null && audio == null) {
			return sendRequest(comment);
		}

        try {

        	HttpHeaders headers = new HttpHeaders();
        	headers.set("Authorization", "Bearer " + this.access_token);

			List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
            acceptableMediaTypes.add(MediaType.MULTIPART_FORM_DATA);
            headers.setAccept(acceptableMediaTypes);

            MultiValueMap<String, Object> valueMap = new LinkedMultiValueMap<String, Object>();

            if (comment != null) {
            	String message = comment.toJSONString();
        		// log sending message
            	logger.info("Send: " + message);

    			valueMap.add("payload", message);
            }
            if (audio != null) {
            	valueMap.add("audio", new FileSystemResource(audio));
            }
            if (file != null) {
            	valueMap.add("file", new FileSystemResource(file));
            }

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<MultiValueMap<String, Object>>(valueMap, headers);

    		RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map<String, String>> result = restTemplate.exchange(
            		URL_ENDPOINT, HttpMethod.POST, entity,
                    new ParameterizedTypeReference<Map<String,String>>() {});

        } catch (JsonProcessingException e) {
            logger.error("Invalid message format!", e);
            return false;
        } catch (RestClientException e) {
            logger.error("Error posting message!", e);
            return false;
        }

        return true;
	}


	public String getBinderInfo() {

		RestTemplate restTemplate = new RestTemplate();

        try {

        	HttpHeaders headers = new HttpHeaders();
        	headers.set("Authorization", "Bearer " + this.access_token);
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<String> entity = new HttpEntity<>(headers);

        	ResponseEntity<String> response = restTemplate.exchange(URL_ENDPOINT + "/binderinfo", HttpMethod.GET, entity, String.class);

            return response.getBody();

        } catch (RestClientException e) {
            logger.error("Error getting binderinfo!", e);
            return null;
        }
	}


	public String getUserInfo() {

		RestTemplate restTemplate = new RestTemplate();

        try {

        	HttpHeaders headers = new HttpHeaders();
        	headers.set("Authorization", "Bearer " + this.access_token);
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<String> entity = new HttpEntity<>(headers);

        	ResponseEntity<String> response = restTemplate.exchange(URL_ENDPOINT + "/userinfo/" + this.user_id, HttpMethod.GET, entity, String.class);

            return response.getBody();

        } catch (RestClientException e) {
            logger.error("Error getting userinfo!", e);
            return null;
        }
	}


	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getBinder_id() {
		return binder_id;
	}

	public void setBinder_id(String binder_id) {
		this.binder_id = binder_id;
	}

	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	public ChatMessage getChatMessage() {
		return chatMessage;
	}

	public void setChatMessage(ChatMessage chatMessage) {

		// log receiving message
		try {
			logger.info("Receive: " + new ObjectMapper().writeValueAsString(chatMessage));
		} catch (Exception e) {}

		this.chatMessage = chatMessage;
		if (chatMessage != null) {
			this.access_token = chatMessage.getAccess_token();
			this.binder_id = chatMessage.getBinder_id();

			switch(chatMessage.getMessage_type()) {
			case "bot_installed":
				eventType = EventType.BOT_INSTALLED;
				break;
			case "bot_uninstalled":
				eventType = EventType.BOT_UNINSTALLED;
				break;
			case "comment_posted":
			case "comment_posted_on_page":
				eventType = EventType.MESSAGE;
				break;
			case "bot_postback":
				eventType = EventType.POSTBACK;
				break;
			case "file_uploaded":
				eventType = EventType.FILE_UPLOADED;
				break;
			case "page_annotated":
				eventType = EventType.PAGE_ANNOTATED;
				break;
			case "todo_created":
				eventType = EventType.TODO_CREATED;
				break;
			case "todo_completed":
				eventType = EventType.TODO_COMPLETED;
				break;
			case "meet_recording_ready":
				eventType = EventType.MEET_RECORDING_READY;
				break;
			}

			if (chatMessage.getEvent() != null) {
				EventUser user = chatMessage.getEvent().getUser();

				if (user != null) {
					this.user_id = user.getId();
					this.username = user.getName();
				}
			}
		}
	}

	public EventBot getBot() {
		if (chatMessage != null && eventType == EventType.BOT_INSTALLED) {
			return chatMessage.getEvent().getBot();
		}
		return null;
	}

	public EventComment getComment() {
		if (chatMessage != null && eventType == EventType.MESSAGE) {
			return chatMessage.getEvent().getComment();
		}
		return null;
	}

	public EventPostback getPostback() {
		if (chatMessage != null && eventType == EventType.POSTBACK) {
			return chatMessage.getEvent().getPostback();
		}
		return null;
	}

	public EventFile getFile() {
		if (chatMessage != null && eventType == EventType.FILE_UPLOADED) {
			return chatMessage.getEvent().getFile();
		}
		return null;
	}

	public EventAnnotate getAnnotate() {
		if (chatMessage != null && eventType == EventType.PAGE_ANNOTATED) {
			return chatMessage.getEvent().getAnnotate();
		}
		return null;
	}

	public EventPage getPage() {
		if (chatMessage != null && eventType == EventType.PAGE_CREATED) {
			return chatMessage.getEvent().getPage();
		}
		return null;
	}

	public EventTodo getTodo() {
		if (chatMessage != null && (eventType == EventType.TODO_CREATED || eventType == EventType.TODO_COMPLETED)) {
			return chatMessage.getEvent().getTodo();
		}
		return null;
	}

	public EventMeet getMeet() {
		if (chatMessage != null && eventType == EventType.MEET_RECORDING_READY) {
			return chatMessage.getEvent().getMeet();
		}
		return null;
	}

	public EventUser getUser() {
		if (chatMessage != null) {
			return chatMessage.getEvent().getUser();
		}
		return null;
	}

	public EventTarget getTarget() {
		if (chatMessage != null) {
			return chatMessage.getEvent().getTarget();
		}
		return null;
	}
}
