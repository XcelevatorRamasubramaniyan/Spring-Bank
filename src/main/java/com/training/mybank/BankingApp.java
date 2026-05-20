package com.training.mybank;

import com.training.mybank.config.AppConfig;
import com.training.mybank.ui.MenuUI;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class BankingApp {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        MenuUI menuUI = context.getBean(MenuUI.class);
        menuUI.start();
        context.close();
    }
}
