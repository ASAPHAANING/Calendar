package org.strangeway.electronvaadin.app;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonString;


import java.io.IOException;
import java.util.List;

/**
 * @author Yuriy Artamonov
 */
@PreserveOnRefresh
@Theme(ValoTheme.THEME_NAME)
@Push
public class MainUI extends UI {

    private Grid tasksGrid;
    private Label pushlbltest = new Label("Push");

    @Override
    protected void init(VaadinRequest request) {
        initLayout();
        initElectronApi();
        new PushTestThread().start();

    }

    private void initLayout() {
        VerticalLayout layout = new VerticalLayout();

        layout.addComponent(pushlbltest);
        layout.addComponent(new CalendarTest());


        setContent(layout);




}

    private void initElectronApi() {
        JavaScript js = getPage().getJavaScript();
        js.addFunction("appMenuItemTriggered", arguments -> {
            if (arguments.length() == 1 && arguments.get(0) instanceof JsonString) {
                String menuId = arguments.get(0).asString();
                if ("About".equals(menuId)) {
                    onMenuAbout();
                } else if ("Exit".equals(menuId)) {
                    onWindowExit();
                }
            }
        });
        js.addFunction("appWindowExit", arguments -> onWindowExit());
    }

    private void callElectronUiApi(String[] args) {
        JsonArray paramsArray = Json.createArray();
        int i = 0;
        for (String arg : args) {
            paramsArray.set(i, Json.create(arg));
            i++;
        }
        getPage().getJavaScript().execute("callElectronUiApi(" + paramsArray.toJson() + ")");
    }

    private void onMenuAbout() {
        Window helpWindow = new Window();
        helpWindow.setCaption("About");
        helpWindow.setModal(true);
        helpWindow.setResizable(false);

        helpWindow.setSizeUndefined();

        VerticalLayout content = new VerticalLayout();
        content.setSizeUndefined();
        content.setMargin(true);
        content.setSpacing(true);

        Label aboutLabel = new Label("Electron+Vaadin Demo\nAuthor: Yuriy Artamonov");
        aboutLabel.setContentMode(ContentMode.PREFORMATTED);
        aboutLabel.setSizeUndefined();

        content.addComponent(aboutLabel);

        Button okBtn = new Button("Ok", FontAwesome.CHECK);
        okBtn.focus();
        okBtn.addClickListener(event -> helpWindow.close());

        content.addComponent(okBtn);
        content.setComponentAlignment(okBtn, Alignment.MIDDLE_CENTER);

        helpWindow.setContent(content);

        getUI().addWindow(helpWindow);
    }

    private void onWindowExit() {
        if (!getUI().getWindows().isEmpty()) {
            // it seems that confirmation window is already shown
            return;
        }

        Window confirmationWindow = new Window();
        confirmationWindow.setResizable(false);
        confirmationWindow.setModal(true);
        confirmationWindow.setCaption("Exit confirmation");
        confirmationWindow.setSizeUndefined();

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setMargin(true);
        layout.setWidthUndefined();

        Label confirmationText = new Label("Are you sure?");
        confirmationText.setSizeUndefined();
        layout.addComponent(confirmationText);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);

        Button yesBtn = new Button("Yes", FontAwesome.SIGN_OUT);
        yesBtn.focus();
        yesBtn.addClickListener(event -> {
            confirmationWindow.close();
            callElectronUiApi(new String[]{"exit"});
        });
        buttonsLayout.addComponent(yesBtn);

        Button noBtn = new Button("No", FontAwesome.CLOSE);
        noBtn.addClickListener(event -> confirmationWindow.close());
        buttonsLayout.addComponent(noBtn);

        layout.addComponent(buttonsLayout);

        confirmationWindow.setContent(layout);

        getUI().addWindow(confirmationWindow);
    }

    class PushTestThread extends Thread {
        int count = 0;

        @Override
        public void run() {
            try {
                // Update the data for a while
                while (count < 100) {
                    Thread.sleep(1000);

                    access(new Runnable() {
                        @Override
                        public void run() {
                           pushlbltest.setValue(""+count++);
                        }
                    });
                }

                // Inform that we have stopped running
                access(new Runnable() {
                    @Override
                    public void run() {
                        pushlbltest.setValue("Done!");
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}