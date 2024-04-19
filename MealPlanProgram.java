/**
 * Class: CSE460 Spring 2024 #11379
 * Description: JavaFX Object-oriented program that replicates Publisher/Subscriber events, using a message broker so the two don't interact directly with one another
 * Author: Robert DeLucia Jr.
 * Date: April 18, 2024
 * ASUID: 1218933794
 */

package com.example.demo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MealPlanProgram extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        // Initializing message broker
        Broker broker = new Broker();

        // Publisher section
        VBox publisherBox = new VBox(10);
        publisherBox.setPadding(new Insets(10));
        publisherBox.setAlignment(Pos.TOP_CENTER);
        Label publisherLabel = new Label("Publisher");
        TextField publisherUsernameField = new TextField();
        publisherUsernameField.setPromptText("Username");
        TextField publisherCuisineTypeField = new TextField();
        publisherCuisineTypeField.setPromptText("Cuisine Type");
        TextField publisherMealNameField = new TextField();
        publisherMealNameField.setPromptText("Meal Name");
        TextField publisherCookTimeField = new TextField();
        publisherCookTimeField.setPromptText("Cook Time");
        TextField publisherTimeField = new TextField();
        publisherTimeField.setPromptText("Time of Meal");
        TextField publisherDayField = new TextField();
        publisherDayField.setPromptText("Day of Meal");
        Button publishButton = new Button("Publish");

        publisherBox.getChildren().addAll(
                publisherLabel, publisherUsernameField, publisherCuisineTypeField,
                publisherMealNameField, publisherCookTimeField, publisherTimeField, publisherDayField, publishButton
        );

        // Subscriber section
        VBox subscriberBox = new VBox(10);
        subscriberBox.setPadding(new Insets(10));
        subscriberBox.setAlignment(Pos.TOP_CENTER);
        Label subscriberLabel = new Label("Subscriber");
        TextField subscriberUsernameField = new TextField();
        subscriberUsernameField.setPromptText("Username");
        TextField subscriberCuisineTypeField = new TextField();
        subscriberCuisineTypeField.setPromptText("Cuisine Type");
        CheckBox weeklyCheckBox = new CheckBox("Weekly");
        CheckBox dailyCheckBox = new CheckBox("Daily");
        Button subscribeButton = new Button("Subscribe");
        Button unsubscribeButton = new Button("Unsubscribe");

        subscriberBox.getChildren().addAll(
                subscriberLabel, subscriberUsernameField, subscriberCuisineTypeField,
                weeklyCheckBox, dailyCheckBox, subscribeButton, unsubscribeButton
        );

        // Message display section
        TextArea messageArea = new TextArea();
        messageArea.setEditable(false);
        messageArea.setWrapText(true);
        HBox messageBox = new HBox(10);
        messageBox.getChildren().add(messageArea);
        HBox.setHgrow(messageArea, Priority.ALWAYS);

        // Main layout
        VBox root = new VBox(20);
        root.setPadding(new Insets(10));
        root.getChildren().addAll(new HBox(10, publisherBox, subscriberBox), messageBox);

        // Subscribe event handling
        subscribeButton.setOnAction(e ->
        {
            // usernames aren't case-sensitive, per the document's instructions
            String subscriberUsername = subscriberUsernameField.getText().toLowerCase();

            if(subscriberUsername.isEmpty())
                messageArea.appendText("Enter the subscriber's username\n");
            else if(broker.subscriberExists(subscriberUsername))
                messageArea.appendText("Username already taken.\n");
            else
            {
                // make a new subscriber if one with the same name doesn't exist
                Subscriber subscriber = new Subscriber(broker, subscriberUsername);
                String message = "subscribe, " + subscriberUsername + ", ";

                // if cuisine field is only field filled out, subscribe to that cuisineType
                if(!subscriberCuisineTypeField.getText().isEmpty() && (!weeklyCheckBox.isSelected() && !dailyCheckBox.isSelected()))
                {
                    messageArea.appendText(message + subscriberCuisineTypeField.getText() + "\n");
                    subscriber.subscribeToCuisine(subscriberCuisineTypeField.getText());
                }

                // if either check box is selected when a cuisine type is specified, tell user that only one type is allowed to be subscribed to
                if ((weeklyCheckBox.isSelected() || dailyCheckBox.isSelected()) && !subscriberCuisineTypeField.getText().isEmpty())
                    messageArea.appendText("Must choose one type to subscribe to.\n");
                else
                {
                    // if both boxes are selected, tell user to only select one
                    if(weeklyCheckBox.isSelected() && dailyCheckBox.isSelected())
                        messageArea.appendText("Must choose one type to subscribe to.\n");
                    else
                    {
                        // handle subscriptions for either mealPlanType
                        if (weeklyCheckBox.isSelected())
                        {
                            messageArea.appendText(message + "weeklyPlan\n");
                            subscriber.subscribeToMealPlan("weeklyPlan");
                        }

                        if (dailyCheckBox.isSelected())
                        {
                            messageArea.appendText( message + "dailyPlan\n");
                            subscriber.subscribeToMealPlan("dailyPlan");
                        }
                    }
                }

                // if user doesn't select anything, it will default subscribe them to mealIdeas, a.k.a all mealPlans that aren't weekly or daily
                if (!dailyCheckBox.isSelected() && !weeklyCheckBox.isSelected() && subscriberCuisineTypeField.getText().isEmpty())
                {
                    messageArea.appendText(message + "mealIdea\n");
                    subscriber.subscribeToMealPlan("mealIdea");
                }
            }
        });

        // Unsubscribe Event handling
        unsubscribeButton.setOnAction(e ->
        {
            String subscriberUsername = subscriberUsernameField.getText().toLowerCase();
            Subscriber subscriber = broker.getSubscriber(subscriberUsername);

            if(subscriberUsername.isEmpty())
                messageArea.appendText("Enter the subscriber's username.\n");
            else if(subscriber == null)
                messageArea.appendText("User by that name doesn't exist in the system.\n");
            else
            {
                String message = "unsubscribe, " + subscriberUsername + ", ";

                // if cuisine field is filled out and checkboxes are empty, try to unsubscribe.
                if (!subscriberCuisineTypeField.getText().isEmpty() && (!dailyCheckBox.isSelected()) && !weeklyCheckBox.isSelected())
                {
                    if (broker.unsubscribeFromCuisineType(subscriber, subscriberCuisineTypeField.getText()))
                        messageArea.appendText(message + subscriberCuisineTypeField.getText() + "\n");
                    else
                        messageArea.appendText("User " + subscriberUsername + " was not subscribed to: " + subscriberCuisineTypeField.getText() + "; Unsubscribe unsuccessful. \n");
                }

                // print out error if both check boxes are selected
                if (weeklyCheckBox.isSelected() && dailyCheckBox.isSelected())
                    messageArea.appendText("Must choose one option between weekly and daily. \n");
                else
                {
                    // handle corresponding unsubscribe for corresponding checkbox
                    if (weeklyCheckBox.isSelected())
                    {
                        if (broker.unsubscribeFromMealPlanType(subscriber, "weeklyPlan"))
                            messageArea.appendText( message + "weeklyPlan" + "\n");
                        else
                            messageArea.appendText("User " + subscriberUsername + " was not subscribed to: weeklyPlan; Unsubscribe unsuccessful. \n");
                    }

                    if (dailyCheckBox.isSelected())
                    {
                        if (broker.unsubscribeFromMealPlanType(subscriber, "dailyPlan"))
                            messageArea.appendText(message + "dailyPlan" + "\n");
                        else
                            messageArea.appendText("User " + subscriberUsername + " was not subscribed to: dailyPlan; Unsubscribe unsuccessful. \n");
                    }
                }

                // if no fields are filled out, unsubscribe from mealIdeas
                if (!dailyCheckBox.isSelected() && !weeklyCheckBox.isSelected() && subscriberCuisineTypeField.getText().isEmpty()) {
                    if (broker.unsubscribeFromMealPlanType(subscriber, "mealIdea"))
                        messageArea.appendText( message + "mealIdea" + "\n");
                    else
                        messageArea.appendText("User " + subscriberUsername + " was not subscribed to: mealIdea; Unsubscribe unsuccessful. \n");
                }
            }
        });

        // Publish Event handling
        publishButton.setOnAction(e ->
        {
            String publisherName = publisherUsernameField.getText().toLowerCase();

            // checking that required fields are filled
            if(publisherUsernameField.getText().isEmpty())
                messageArea.appendText("Publisher needs a username!\n");
            else if(publisherCuisineTypeField.getText().isEmpty())
                messageArea.appendText("Publisher needs to specify cuisine\n");
            else if(publisherMealNameField.getText().isEmpty())
                messageArea.appendText("Publisher needs to specify the meal's name\n");
            else if(publisherCookTimeField.getText().isEmpty())
                messageArea.appendText("Publisher needs to specify the meal's cook time\n");
            else
            {
                Publisher publisher = new Publisher(broker, publisherName);
                String message = "publish, " + publisherName + ", ";
                String cuisineType = publisherCuisineTypeField.getText();
                String mealName = publisherMealNameField.getText();
                String cookTime = publisherCookTimeField.getText();

                // optional fields
                String timeOfDay = "";
                String dayOfWeek = "";
                boolean timeGiven = false;
                boolean dayGiven = false;

                if(!publisherTimeField.getText().isEmpty())
                {
                    timeOfDay = publisherTimeField.getText();
                    timeGiven = true;
                }

                if(!publisherDayField.getText().isEmpty())
                {
                    dayOfWeek = publisherDayField.getText();
                    dayGiven = true;
                }

                // if time and day not given, make a mealIdea-type MealPlan object to publish
                if(!timeGiven && !dayGiven)
                {
                    messageArea.appendText(message + "mealIdea, " + mealName + ", " + cuisineType + ", " + cookTime + "\n");
                    MealPlan mealIdeaPlan = new MealPlan(cuisineType, mealName, cookTime);
                    publisher.publishMealPlan(mealIdeaPlan);

                    List<String> subscribersByMealPlan = broker.getSubscribersForMealPlanType("mealIdea");
                    List<String> subscribersByCuisine = broker.getSubscribersForCuisineType(cuisineType);
                    for(String subscriberName : subscribersByMealPlan)
                        messageArea.appendText(subscriberName + " was notified of publish event\n");
                    for(String subscriberName : subscribersByCuisine)
                        messageArea.appendText(subscriberName + " was notified of publish event\n");

                }
                // if time is given but not day, make a dailyPlan-type MealPlan object to publish
                else if(!dayGiven && timeGiven)
                {
                    messageArea.appendText(message + "dailyPlan, " + mealName + ", " + cuisineType + ", " + cookTime + ", " + timeOfDay + "\n");
                    MealPlan dailyMealPlan = new DailyMealPlan(cuisineType, mealName, cookTime, timeOfDay);
                    publisher.publishMealPlan(dailyMealPlan);

                    List<String> subscribersByMealPlan = broker.getSubscribersForMealPlanType("dailyPlan");
                    List<String> subscribersByCuisine = broker.getSubscribersForCuisineType(cuisineType);
                    for(String subscriberName : subscribersByMealPlan)
                        messageArea.appendText(subscriberName + " was notified of publish event\n");
                    for(String subscriberName : subscribersByCuisine)
                        messageArea.appendText(subscriberName + " was notified of publish event\n");
                }
                // if day is given but not time, make a weeklyPlan-type MealPlan object to publish
                else if(dayGiven && !timeGiven)
                {
                    messageArea.appendText(message + "weeklyPlan, " + mealName + ", " + cuisineType + ", " + cookTime + ", " + dayOfWeek + "\n");
                    MealPlan weeklyMealPlan = new WeeklyMealPlan(cuisineType, mealName, cookTime, dayOfWeek);
                    publisher.publishMealPlan(weeklyMealPlan);

                    List<String> subscribersByMealPlan = broker.getSubscribersForMealPlanType("weeklyPlan");
                    List<String> subscribersByCuisine = broker.getSubscribersForCuisineType(cuisineType);
                    for(String subscriberName : subscribersByMealPlan)
                        messageArea.appendText(subscriberName + " was notified of publish event\n");
                    for(String subscriberName : subscribersByCuisine)
                        messageArea.appendText(subscriberName + " was notified of publish event\n");
                }
                // if everything above doesn't pass, that means time and day were given, make a weeklyPlan-type MealPlan object
                else
                {
                    messageArea.appendText(message + "weeklyPlan, " + mealName + ", " + cuisineType + ", " + cookTime + ", " + timeOfDay + ", " + dayOfWeek + "\n");
                    MealPlan weeklyMealPlan = new WeeklyMealPlan(cuisineType, mealName, cookTime, timeOfDay, dayOfWeek);
                    publisher.publishMealPlan(weeklyMealPlan);

                    List<String> subscribersByMealPlan = broker.getSubscribersForMealPlanType("weeklyPlan");
                    List<String> subscribersByCuisine = broker.getSubscribersForCuisineType(cuisineType);
                    for(String subscriberName : subscribersByMealPlan)
                        messageArea.appendText(subscriberName + " was notified of publish event\n");
                    for(String subscriberName : subscribersByCuisine)
                        messageArea.appendText(subscriberName + " was notified of publish event\n");
                }
            }
        });

        // Scene setup
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Publisher Subscriber App");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    // Classes
    class Subscriber
    {
        private List<String> cuisineTypes;
        private Broker broker;
        private List<MealPlan> storedMealPlans;
        private String subscriberName;
        private List<String> mealPlanTypes;

        public Subscriber(Broker broker, String name)
        {
            this.broker = broker;
            this.subscriberName = name;
            this.cuisineTypes = new ArrayList<>();
            this.mealPlanTypes = new ArrayList<>();
            this.storedMealPlans = new ArrayList<>();
        }

        public void subscribeToCuisine(String cuisineType)
        {
            cuisineTypes.add(cuisineType);
            broker.subscribeToCuisineType(this, cuisineType);
        }

        public void saveMealPlan(MealPlan mealPlan)
        {
            storedMealPlans.add(mealPlan);
        }

        public List<MealPlan> getSavedPlans() { return storedMealPlans; }

        public void subscribeToMealPlan(String mealPlanType)
        {
            mealPlanTypes.add(mealPlanType);
            broker.subscribeToMealPlanType(this, mealPlanType);
        }

        public void unsubscribeFromMealPlan(String mealPlanType)
        {
            mealPlanTypes.remove(mealPlanType);
            broker.unsubscribeFromMealPlanType(this, mealPlanType);
        }

        public void unsubscribeFromCuisine(String cuisineType)
        {
            cuisineTypes.remove(cuisineType);
            broker.unsubscribeFromCuisineType(this, cuisineType);
        }

        public String getUsername(){ return subscriberName; }

    }

    class Broker
    {
        private Map<String, List<Subscriber>> subscribersCuisineTypeMap;
        private Map<String, List<Subscriber>> subscribersMealTypeMap;

        public void publish(MealPlan mealPlan, String publisherName)
        {
            String cuisineType = mealPlan.getCuisineType();
            String mealPlanType = mealPlan.getMealPlanType();
            mealPlan.setPublisherName(publisherName);

            // get list of subscribers subscribed to both the cuisineType and mealPlanType
            List<Subscriber> cuisineSubscribers = subscribersCuisineTypeMap.get(cuisineType);
            List<Subscriber> mealPlanSubscribers = subscribersMealTypeMap.get(mealPlanType);

            // save mealPlans to corresponding subscriber's list of meal plans
            if (cuisineSubscribers != null) {
                for (Subscriber subscriber : cuisineSubscribers) {
                    subscriber.saveMealPlan(mealPlan);
                }
            }

            if (mealPlanSubscribers != null) {
                for (Subscriber subscriber : mealPlanSubscribers) {
                    subscriber.saveMealPlan(mealPlan);
                }
            }
        }

        public void subscribeToCuisineType(Subscriber subscriber, String cuisineType)
        {
            if (!subscribersCuisineTypeMap.containsKey(cuisineType)) {
                subscribersCuisineTypeMap.put(cuisineType, new ArrayList<>());
            }
            subscribersCuisineTypeMap.get(cuisineType).add(subscriber);
        }

        public Broker()
        {
            subscribersCuisineTypeMap = new HashMap<>();
            subscribersMealTypeMap= new HashMap<>();
        }

        public void subscribeToMealPlanType(Subscriber subscriber, String mealPlanType)
        {
            if (!subscribersMealTypeMap.containsKey(mealPlanType)) {
            subscribersMealTypeMap.put(mealPlanType, new ArrayList<>());
        }
            subscribersMealTypeMap.get(mealPlanType).add(subscriber);
        }

        // returns true after checking if subscriber exists with given cuisineType and then removing them
        public boolean unsubscribeFromCuisineType(Subscriber subscriber, String cuisineType)
        {
            List<Subscriber> subscribers = subscribersCuisineTypeMap.get(cuisineType);
            if (subscribers != null && subscribers.contains(subscriber)) {
                subscribers.remove(subscriber);
                return true;
            }
            return false;
        }

        public boolean unsubscribeFromMealPlanType(Subscriber subscriber, String mealPlanType)
        {
            List<Subscriber> subscribers = subscribersMealTypeMap.get(mealPlanType);
            if (subscribers != null && subscribers.contains(subscriber)) {
                subscribers.remove(subscriber);
                return true;
            }
            return false;
        }

        // returns true if subscriber exists in either hash map
        public boolean subscriberExists(String username) {
            for (List<Subscriber> subscriberList : subscribersCuisineTypeMap.values()) {
                for (Subscriber subscriber : subscriberList) {
                    if (subscriber.getUsername().equals(username)) {
                        return true;
                    }
                }
            }

            for (List<Subscriber> subscriberList : subscribersMealTypeMap.values()) {
                for (Subscriber subscriber : subscriberList) {
                    if (subscriber.getUsername().equals(username)) {
                        return true;
                    }
                }
            }

            return false;
        }

        // returns subscriber object with same name as the one passed. if not, returns null
        public Subscriber getSubscriber(String name) {
            // Check subscribers in subscribersCuisineTypeMap
            for (List<Subscriber> subscriberList : subscribersCuisineTypeMap.values()) {
                for (Subscriber subscriber : subscriberList) {
                    if (subscriber.getUsername().equals(name)) {
                        return subscriber;
                    }
                }
            }

            // Check subscribers in subscribersMealTypeMap
            for (List<Subscriber> subscriberList : subscribersMealTypeMap.values()) {
                for (Subscriber subscriber : subscriberList) {
                    if (subscriber.getUsername().equals(name)) {
                        return subscriber;
                    }
                }
            }

            // Subscriber not found
            return null;
        }

        // returns a list of names of subscribers subscribed to the given type
        public List<String> getSubscribersForCuisineType(String cuisineType) {
            List<String> subscribersList = new ArrayList<>();
            List<Subscriber> subscribers = subscribersCuisineTypeMap.get(cuisineType);
            if (subscribers != null) {
                for (Subscriber subscriber : subscribers) {
                    subscribersList.add(subscriber.getUsername());
                }
            }
            return subscribersList;
        }

        public List<String> getSubscribersForMealPlanType(String mealPlanType) {
            List<String> subscribersList = new ArrayList<>();
            List<Subscriber> subscribers = subscribersMealTypeMap.get(mealPlanType);
            if (subscribers != null) {
                for (Subscriber subscriber : subscribers) {
                    subscribersList.add(subscriber.getUsername());
                }
            }
            return subscribersList;
        }
    }

    class Publisher
    {
        private String publisherName;
        private Broker broker;

        public Publisher(Broker broker, String name)
        {
            this.broker = broker;
            this.publisherName = name;
        }

        public void publishMealPlan(MealPlan mealPlan)
        {
            broker.publish(mealPlan, getPublisherName());
        }

        public String getPublisherName(){ return publisherName; }

    }

    class MealPlan
    {
        private String cuisineType;
        private String mealName;
        private String mealType;
        private String cookTime;
        private String mealPlanType;
        private String publisherName;

        public MealPlan(String cuisineType, String mealName, String cookTime){ mealPlanType = "mealIdea";}

        public String getCuisineType() {
            return cuisineType;
        }

        public String getMealName() {
            return mealName;
        }

        public String getMealType() {
            return mealType;
        }

        public void setCuisineType(String cuisineType) { this.cuisineType = cuisineType; }

        public void setMealName(String mealName) { this.mealName = mealName; }

        public void setMealType(String mealType) { this.mealType = mealType; }

        public void setCookTime(String cookTime) { this.cookTime = cookTime; }

        public String getCookTime() {
            return cookTime;
        }

        public void setMealPlanType(String mealPlanType) { this.mealPlanType = mealPlanType; }

        public String getMealPlanType() {
            return mealPlanType;
        }

        public void setPublisherName(String name) { this.publisherName = name; }

        public String getPublisherName() { return publisherName; }

    }

    class DailyMealPlan extends MealPlan
    {
        private String timeOfDay;

        public DailyMealPlan(String cuisineType, String mealName, String cookTime, String timeOfDay)
        {
            super(cuisineType, mealName, cookTime);
            this.timeOfDay = timeOfDay;
            setMealPlanType("dailyPlan");
        }

        public String getTimeOfDay() {
            return timeOfDay;
        }

        public void setTimeOfDay(String timeOfDay) { this.timeOfDay = timeOfDay; }

    }

    class WeeklyMealPlan extends DailyMealPlan
    {
        private String dayOfWeek;

        public String getDayOfWeek() {
            return dayOfWeek;
        }

        // constructor for when timeOfDay isn't passed
        public WeeklyMealPlan(String cuisineType, String mealName, String cookTime, String dayOfWeek) {
            super(cuisineType, mealName, cookTime,null);
            this.dayOfWeek = dayOfWeek;
        }

        public WeeklyMealPlan(String cuisineType, String mealName, String cookTime, String timeOfDay, String dayOfWeek) {
            super(cuisineType, mealName, cookTime, timeOfDay);
            this.dayOfWeek = dayOfWeek;
        }

    }
}