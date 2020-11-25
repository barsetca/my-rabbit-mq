package ru.cherniak.produser;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.SerializationUtils;


public class ExchangeSenderApp {

  private static final String EXCHANGE_NAME = "myTopicExchanger";

  private static final List<String> PROGRAMMING_LANGUAGE = new ArrayList<>();
  private static final List<String> PROGRAMMING_LANGUAGE_SECTION = new ArrayList<>();

  static {
    PROGRAMMING_LANGUAGE.add("java");
    PROGRAMMING_LANGUAGE.add("js");
    PROGRAMMING_LANGUAGE.add("html");

    PROGRAMMING_LANGUAGE_SECTION.add("common");
    PROGRAMMING_LANGUAGE_SECTION.add("backend");
    PROGRAMMING_LANGUAGE_SECTION.add("frontend");
  }

  public static void main(String[] argv) throws Exception {
    while (true) {
      System.out.println("Введите цифру соответвующий языку программирования статьи");
      for (int i = 0; i < PROGRAMMING_LANGUAGE.size(); i++) {
        int k = i + 1;
        System.out.printf("%d - %s%n", k, PROGRAMMING_LANGUAGE.get(i));
      }
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      String langIndex = reader.readLine().trim();
      String lang = PROGRAMMING_LANGUAGE.get(Integer.parseInt(langIndex) - 1);
      System.out.println("Введите цифру соответвующую разделу, выбранного языка статьи");
      for (int i = 0; i < PROGRAMMING_LANGUAGE_SECTION.size(); i++) {
        int k = i + 1;
        System.out.printf("%d - %s%n", k, PROGRAMMING_LANGUAGE_SECTION.get(i));
      }
      String sectionIndex = reader.readLine().trim();
      String section = PROGRAMMING_LANGUAGE_SECTION.get(Integer.parseInt(sectionIndex) - 1);
      StringBuilder sb = new StringBuilder();
      sb.append("programming.").append(section).append(".").append(lang);
      System.out.println("ВВедите полный путь файла для публикации");
      String filePath = reader.readLine().trim();
      publishArticle(sb.toString(), filePath);
    }
  }

  private static void publishArticle(String theme, String filePath) throws Exception{
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    try (Connection connection = factory.newConnection();
        Channel channel = connection.createChannel()) {
      channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

      channel.basicPublish(EXCHANGE_NAME, theme, null,
          SerializationUtils.serialize(new MyMessage(filePath)));
      System.out.println(" [x] Sent '" + theme + "'");
      System.out.println(" [x] Sent '" + filePath + "'");
    }
  }
}