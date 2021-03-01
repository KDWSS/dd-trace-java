package datadog.trace.instrumentation.jms;

import datadog.trace.api.Config;
import datadog.trace.api.Function;
import datadog.trace.api.cache.DDCache;
import datadog.trace.api.cache.DDCaches;
import datadog.trace.bootstrap.instrumentation.api.AgentPropagation;
import java.util.Enumeration;
import javax.jms.JMSException;
import javax.jms.Message;

import de.thetaphi.forbiddenapis.SuppressForbidden;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageExtractAdapter implements AgentPropagation.ContextVisitor<Message> {

  @SuppressForbidden
  private static final boolean USE_LEGACY_DASH_REPLACEMENT =
      Config.get().isJmsLegacyDashReplacement();

  private static final Function<String, String> KEY_MAPPER =
      new Function<String, String>() {
        @Override
        public String apply(String key) {
          if (USE_LEGACY_DASH_REPLACEMENT) {
            return key.replace("__dash__", "-").toLowerCase();
          }
          return key.replace('$', '-').toLowerCase();
        }
      };

  private final DDCache<String, String> cache = DDCaches.newFixedSizeCache(32);

  public static final MessageExtractAdapter GETTER = new MessageExtractAdapter();

  @Override
  public void forEachKey(Message carrier, AgentPropagation.KeyClassifier classifier) {
    try {
      final Enumeration<?> enumeration = carrier.getPropertyNames();
      if (null != enumeration) {
        while (enumeration.hasMoreElements()) {
          String key = ((String) enumeration.nextElement());
          String lowerCaseKey = cache.computeIfAbsent(key, KEY_MAPPER);
          Object value = carrier.getObjectProperty(key);
          if (value instanceof String && !classifier.accept(lowerCaseKey, (String) value)) {
            return;
          }
        }
      }
    } catch (JMSException e) {
      throw new RuntimeException(e);
    }
  }
}
