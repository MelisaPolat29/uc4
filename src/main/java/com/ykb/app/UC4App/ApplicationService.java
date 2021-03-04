package com.ykb.app.UC4App;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Base64;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.http.client.ClientProtocolException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ApplicationService {

	public static void main(String args[]) throws ClientProtocolException, IOException, URISyntaxException {
		basicAuth();
	}

	public static void basicAuth() {
		try {
			RestTemplate restTemplate = new RestTemplate();
			URI url = new URI("https://sdlc.yapikredi.com.tr/jira/login.jsp");
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "Basic xxxxxx");
			HttpEntity request = new HttpEntity(headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
			if (response.getStatusCodeValue() == 200) {
				System.out.println("Login Success!");
				serviceApi(request);
			}
		} catch (Exception ex) {
			System.out.println("Error in loginToJira: " + ex.getMessage());
		}
	}

	public static void serviceApi(HttpEntity request) throws JsonMappingException, JsonProcessingException {
		RestTemplate restTemplate = new RestTemplate();
		String url = "https://sdlc.yapikredi.com.tr/jira/rest/api/latest/search?jql=project = BATCH AND issuetype = Task AND status = Done ORDER BY key desc&fields=key,summary&maxResults=1000&startAt=0";
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
		System.out.println(response.getStatusCode());

		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(response.getBody());
		JsonNode total = root.path("total");
		Integer loopVal = Integer.valueOf(total.asText());
		JsonNode issue = root.path("issues");
		/*ApiModel model = new ApiModel();
		Issue issues = new Issue();
		Field field = new Field();*/
		//DB
		String jdbcURL = "jdbc:jtds:sqlserver://server_name:port/db_name;instance=instance_name";
		String dbUserName = "xxxxx";
		byte[] dbPassDe = Base64.getDecoder().decode("xxxxx");
		String dbPasswd = new String(dbPassDe);
		
	
		try (Connection connection = DriverManager.getConnection(jdbcURL, dbUserName, dbPasswd)) {
			String delQuery="Delete from MDB.dbo.zJiraUC4Jobs";
			PreparedStatement statement = connection.prepareStatement(delQuery);
			statement.execute();
			for (int i = 0; i < loopVal; i++) {
				//Deneme amacli listeler olusturup icerisine ekledim ki ekleniyormu diye class olusturup!
				/*issues.setId((issue.get(i).get("id")).asText());
				issues.setKey((issue.get(i).get("key")).asText());
				field.setSummary(((issue.get(0).get("fields")).get("summary")).asText());
				Issue[] issuesList = new Issue[] { issues };
				model.setIssues(issuesList);
				issues.setFields(field);*/
				
				String query = "INSERT INTO MDB.dbo.zJiraUC4Jobs (id,[key], summary) VALUES('"+(issue.get(i).get("id")).asText()+"','"+
						(issue.get(i).get("key")).asText()+"','"+((issue.get(i).get("fields")).get("summary")).asText()+"')";
				 PreparedStatement preparedStmt = connection.prepareStatement(query);
				preparedStmt.execute();
			}
			

		} catch (SQLException e) {
			System.out.println("Database error:");
			e.printStackTrace();
		}	
	}

}
