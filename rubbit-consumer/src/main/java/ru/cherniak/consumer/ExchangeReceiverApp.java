package ru.cherniak.consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.SerializationUtils;
import ru.cherniak.produser.ChooseFasade;
import ru.cherniak.produser.MyMessage;

public class ExchangeReceiverApp {

  private static final String EXCHANGE_NAME = "myTopicExchanger";
  private static final String CHOOSE_PROGRAMMING_LANGUAGE =
      "Введите цифру соответвующий языку программирования статьи";
  private static final String CHOOSE_PROGRAMMING_SECTION =
      "Введите цифру соответвующую разделу, выбранного языка статьи";
  private static final String ERROR =
      "Щшибка! Введено несуществующуу значение! Попробуйте снова.";
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
    ru.cherniak.produser.ChooseFasade chooseFacade = new ChooseFasade();
    while (true) {
      printStartMessage();
      String langSign = chooseFacade.readStringFromConsole();
      if (!"UNBIND".equals(langSign)) {
        if (!"BIND".equals(langSign)) {
          System.out.println(ERROR);
          continue;
        }
      }
      chooseFacade.sendMessageToConsole(CHOOSE_PROGRAMMING_LANGUAGE, PROGRAMMING_LANGUAGE);
      String langIndex = chooseFacade.readStringFromConsole();
      String lang = PROGRAMMING_LANGUAGE.get(Integer.parseInt(langIndex) - 1);
      chooseFacade.sendMessageToConsole(CHOOSE_PROGRAMMING_SECTION, PROGRAMMING_LANGUAGE_SECTION);
      String sectionIndex = chooseFacade.readStringFromConsole();
      String section = PROGRAMMING_LANGUAGE_SECTION.get(Integer.parseInt(sectionIndex) - 1);
      if ("all".equals(section)) {
        section = "#";
      }
      String theme = getTheme(section, lang);
      if ("UNBIND".equals(langSign)) {
        udBind(queueName, channel, theme);
      } else {
        createBind(queueName, channel, theme);
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
          printStartMessage();
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
        });
      } catch (IOException e) {
        e.printStackTrace();
      }
    }).start();
  }

  private static void udBind(String queueName, Channel channel, String section) throws IOException {
    channel.queueUnbind(queueName, EXCHANGE_NAME, section);
    System.out.println(" [*] Вы отписались от канала: " + section);
  }

  private static void printStartMessage() {
    System.out.println("Введите команду BIND, чтобы ПОДПИСАТЬСЯ на новый канал");
    System.out.println("Введите команду UNBIND, чтобы ОТПИСАТЬСЯ от канала");
  }

  public static String getTheme(String section, String lang) {
    StringBuilder sb = new StringBuilder();
    sb.append("programming.").append(section).append(".").append(lang);
    return sb.toString();
  }
}
