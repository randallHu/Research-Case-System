package oil.utils;


import oil.model.User;
import oil.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;

/**
 * Created by  waiter on 18-8-9  上午6:55.
 *
 * @author waiter
 */
@Service
@Transactional(rollbackFor = Exception.class)
@Configuration
public class MailClientService {
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private UserDetailsServiceImpl userDetailsServiceIml;
    @Autowired
    private MailContentBuilder mailContentBuilder;

    @Async
    public void prepareAndSend(String recipient, String message, String subject) {
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("1403976416@qq.com");
            messageHelper.setTo(recipient);
            messageHelper.setSubject(subject);
            messageHelper.setText(message, true);
        };
        try {
            mailSender.send(messagePreparator);
        } catch (MailException e) {
            // runtime exception; compiler will not force you to handle it
            e.printStackTrace();
        }
    }


}
