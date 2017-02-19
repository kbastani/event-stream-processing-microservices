package demo;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.json.JsonPropertyAccessor;

import java.text.ParseException;

public class DateTimeTests {

    @Test
    public void testDateTime() throws ParseException {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

        System.out.print(new SpelExpressionParser().parseRaw("new org.joda.time.DateTime(new java.lang.Long('1487145898898')).toLocalDateTime().toString()").getValue());
        dateFormatter.parseDateTime(new DateTime(new Long("1487145898898")).toLocalDateTime().toString());

        JsonPropertyAccessor.ToStringFriendlyJsonNode jsonNode = new JsonPropertyAccessor.ToStringFriendlyJsonNode(JsonNodeFactory.instance.textNode("test"));

        new SpelExpressionParser().parseExpression("toString()").getValue(jsonNode, CharSequence.class);
    }
}
