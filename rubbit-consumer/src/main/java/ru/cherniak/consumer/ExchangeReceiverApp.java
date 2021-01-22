package ru.cherniak.consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.SerializationUtils;
import ru.cherniak.produser.MyMessage;

public class ExchangeReceiverApp {

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
    PROGRAMMING_LANGUAGE_SECTION.add("all");
  }

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    String queueName = channel.queueDeclare().getQueue();

    while (true) {
      System.out.println("Введите команду BIND, чтобы ПОДПИСАТЬСЯ на новый канал");
      System.out.println("Введите команду UNBIND, чтобы ОТПИСАТЬСЯ от канала");
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      String langSign = reader.readLine().trim().toUpperCase();

      System.out.println(
          "Введите цифру соответвующую языку программирования подписки, а затем выберете раздел");
      for (int i = 0; i < PROGRAMMING_LANGUAGE.size(); i++) {
        int k = i + 1;
        System.out.printf("%d - %s%n", k, PROGRAMMING_LANGUAGE.get(i));
      }

      String langIndex = reader.readLine().trim();
      String lang = PROGRAMMING_LANGUAGE.get(Integer.parseInt(langIndex) - 1);
      if ("all".equals(lang)) {
        System.out.println("Введите цифру соответвующую разделу, выбранного языка статьи");
      }
      for (int i = 0; i < PROGRAMMING_LANGUAGE_SECTION.size(); i++) {
        int k = i + 1;
        System.out.printf("%d - %s%n", k, PROGRAMMING_LANGUAGE_SECTION.get(i));
      }
      String sectionIndex = reader.readLine().trim();
      String section = PROGRAMMING_LANGUAGE_SECTION.get(Integer.parseInt(sectionIndex) - 1);
      if ("all".equals(section)) {
        section = "#";
      }
      StringBuilder sb = new StringBuilder();
      sb.append("programming.").append(section).append(".").append(lang);
      if ("UNBIND".equals(langSign)) {
        channel.queueUnbind(queueName, EXCHANGE_NAME, section);
        System.out.println(" [*] Вы отписались от канала: " + section);
      } else if ("BIND".equals(langSign)) {
        createBind(queueName, channel, sb.toString());
      }
    }
  }

  private static void createBind(String queueName, Channel channel, String section) {
    new Thread(() -> {
      try {
        channel.queueBind(queueName, EXCHANGE_NAME, section);

        System.out.println(" [*] Вы подписаны на канал " + section);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
          MyMessage mm = SerializationUtils.deserialize(delivery.getBody());
          List<String> article = mm.getArticleContent();
          System.out.println(" [x] Received :");
          article.forEach(System.out::println);
          System.out.println("Введите команду BIND, чтобы ПОДПИСАТЬСЯ на новый канал");
          System.out.println("Введите команду UNBIND, чтобы ОТПИСАТЬСЯ от канала");
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
        });
      } catch (IOException e) {
        e.printStackTrace();
      }
    }).start();
  }

}
