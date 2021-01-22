package ru.cherniak.produser;

import static ru.cherniak.produser.UtilCommon.*;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.SerializationUtils;

public class ExchangeSenderApp {

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
    ChooseFasade chooseFacade = new ChooseFasade();
    while (true) {
      chooseFacade.sendMessageToConsole(CHOOSE_PROGRAMMING_LANGUAGE, PROGRAMMING_LANGUAGE);
      String langIndex = chooseFacade.readStringFromConsole();
      chooseFacade.sendMessageToConsole(CHOOSE_PROGRAMMING_SECTION, PROGRAMMING_LANGUAGE_SECTION);
      String sectionIndex = chooseFacade.readStringFromConsole();
      int indexLanguage;
      int indexSection;
      try {
        indexLanguage = Integer.parseInt(langIndex);
        indexSection = Integer.parseInt(sectionIndex);
        if (
            chooseFacade.isOutOfBounds(
                indexLanguage, indexSection, PROGRAMMING_LANGUAGE.size(), PROGRAMMING_LANGUAGE_SECTION.size())
        ) {
          continue;
        }
      } catch (NumberFormatException e) {
        System.out.println(ERROR);
        continue;
      }
      String theme = getTheme(indexLanguage, indexSection);
      System.out.println("Введите полный путь файла для публикации используя разделитель /");
      String filePublisherPath = chooseFacade.readStringFromConsole();
      publishArticle(theme, filePublisherPath);
    }
  }

  private static void publishArticle(String theme, String filePublisherPath) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    try (Connection connection = factory.newConnection();
        Channel channel = connection.createChannel()) {
      channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

      channel.basicPublish(EXCHANGE_NAME, theme, null,
          SerializationUtils.serialize(new MyMessage(Files.readAllLines(Paths.get(filePublisherPath)))));
      System.out.println(" [x] Sent '" + theme + "'");
      System.out.println(" [x] Sent '" + filePublisherPath + "'");
    }
  }

  public static String getTheme(int indexLanguage, int indexSection) {
    String lang = PROGRAMMING_LANGUAGE.get(indexLanguage - 1);
    String section = PROGRAMMING_LANGUAGE_SECTION.get(indexSection - 1);
    StringBuilder sb = new StringBuilder();
    sb.append("programming.").append(section).append(".").append(lang);
    return sb.toString();
  }
}