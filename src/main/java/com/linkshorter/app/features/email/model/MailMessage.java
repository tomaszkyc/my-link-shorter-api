package com.linkshorter.app.features.email.model;

import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Data
@ToString
public class MailMessage {
    private String fromAddress;
    private String fromDisplayName;
    private String to;
    private String subject;
    private String text;

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String to;
        private String subject;
        private String text;
        private Map<String, String> textParameters;

        public Builder to(String to) {
            this.to = to;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Builder text(Path htmlTemplateFilePath) throws Exception {
            String text = Files.readString(htmlTemplateFilePath);
            this.text = text;
            return this;
        }

        public Builder textParameters(Map<String, String> textParameters) {
            this.textParameters = textParameters;
            return this;
        }

        public MailMessage build() {
            if (StringUtils.isBlank(to)) {
                throw new IllegalStateException("To is blank or nullable");
            }
            if (StringUtils.isBlank(subject)) {
                throw new IllegalStateException("Subject is blank or nullable");
            }
            if (StringUtils.isBlank(text)) {
                throw new IllegalStateException("Text is blank or nullable");
            }
            replaceTextParametersInsideText();

            MailMessage mailMessage = new MailMessage();
            mailMessage.setTo(this.to);
            mailMessage.setSubject(this.subject);
            mailMessage.setText(this.text);
            return mailMessage;
        }

        private void replaceTextParametersInsideText() {
            if (StringUtils.isBlank(text)) {
                throw new IllegalStateException("No text found in email");
            }
            if (textParameters != null && textParameters.size() > 0) {
                for (Map.Entry<String, String> textParameter : textParameters.entrySet()) {
                    String key = textParameter.getKey();
                    String value = textParameter.getValue();
                    text = text.replaceAll(key, value);
                }
            }
        }
    }

    public MimeMessagePreparator asMimeMessagePreparator() {
        return buildMimeMessagePreparator();
    }

    private MimeMessagePreparator buildMimeMessagePreparator() {
        return mimeMessage -> {
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            mimeMessage.setFrom(new InternetAddress(fromAddress, fromDisplayName));
            mimeMessage.setSubject(subject);
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setText(text, true);
        };
    }

    private MailMessage() {
    }


}
