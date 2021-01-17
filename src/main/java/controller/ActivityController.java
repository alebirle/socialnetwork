package controller;

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import socialnetwork.domain.*;
import socialnetwork.service.EventService;
import socialnetwork.service.FriendshipService;
import socialnetwork.service.MessageService;
import socialnetwork.service.UserService;

import javax.swing.text.StyleConstants;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

public class ActivityController {
    private UserService usrv;
    private FriendshipService fsrv;
    private MessageService msrv;
    private User logged;
    private Stage primaryStage;
    @FXML
    TableView<UserFriendshipDTO> tableFriends;
    @FXML
    TableView<ConversationDTO> tableMessages;
    @FXML
    TableColumn<UserFriendshipDTO,String> tableColumnFName;
    @FXML
    TableColumn<UserFriendshipDTO,String> tableColumnLName;
    @FXML
    TableColumn<UserFriendshipDTO,String> tableColumnEmail;
    @FXML
    TableColumn<UserFriendshipDTO,String> tableColumnFriendshipDate;
    @FXML
    TableColumn<ConversationDTO,String> tableColumnFrom;
    @FXML
    TableColumn<ConversationDTO,String> tableColumnTo;
    @FXML
    TableColumn<ConversationDTO,String> tableColumnMessage;
    @FXML
    TableColumn<ConversationDTO,String> tableColumnMessageDate;
    @FXML
    TableColumn<ConversationDTO,String> tableColumnReply;
    //@FXML
    //ListView listView;
    @FXML
    DatePicker datepickerFrom;
    @FXML
    DatePicker datepickerTo;
    ObservableList<Tuple<String, LocalDate>> activity= FXCollections.observableArrayList();
    ObservableList<UserFriendshipDTO> friendsActivity=FXCollections.observableArrayList();
    ObservableList<ConversationDTO> messagesActivity=FXCollections.observableArrayList();
    private EventService esrv;

    @FXML
    public void initialize() {
        tableColumnFName.setCellValueFactory(new PropertyValueFactory<UserFriendshipDTO,String>("fname"));
        tableColumnLName.setCellValueFactory(new PropertyValueFactory<UserFriendshipDTO,String>("lname"));
        tableColumnEmail.setCellValueFactory(new PropertyValueFactory<UserFriendshipDTO,String>("email"));
        tableColumnFriendshipDate.setCellValueFactory(new PropertyValueFactory<UserFriendshipDTO,String>("date"));
        tableColumnFrom.setCellValueFactory(new PropertyValueFactory<ConversationDTO,String>("from"));
        tableColumnTo.setCellValueFactory(new PropertyValueFactory<ConversationDTO,String>("to"));
        tableColumnMessage.setCellValueFactory(new PropertyValueFactory<ConversationDTO,String>("message"));
        tableColumnMessageDate.setCellValueFactory(new PropertyValueFactory<ConversationDTO,String>("date"));
        tableColumnReply.setCellValueFactory(new PropertyValueFactory<ConversationDTO,String>("reply"));
        tableMessages.setItems(messagesActivity);
        tableFriends.setItems(friendsActivity);
        //listView.setItems(activity);
        datepickerFrom.setFocusTraversable(false);
        datepickerTo.setFocusTraversable(false);
    }

    public void handleCancel() throws IOException {
        FXMLLoader dbLoader = new FXMLLoader();
        dbLoader.setLocation(getClass().getResource("/views/DashboardView.fxml"));
        AnchorPane dbLayout = dbLoader.load();
        primaryStage.setScene(new Scene(dbLayout));
        primaryStage.setTitle("Your page");
        DashboardController dbController = dbLoader.getController();
        dbController.setFields(logged,usrv,fsrv,msrv,esrv, primaryStage);
    }

    public void handleShow() {
        if(datepickerFrom.getValue()==null||datepickerTo.getValue()==null){
            MessageAlert.showErrorMessage(null,"You must pick an interval!");
        }
        else{
            initActivity();
            LocalDate from=datepickerFrom.getValue();
            LocalDate to=datepickerTo.getValue();
            List<Tuple<String,LocalDate>> filter=new ArrayList<>();
            List<UserFriendshipDTO> filterFriends=new ArrayList<>();
            List<ConversationDTO> filterMessages=new ArrayList<>();
            for(Tuple<String,LocalDate> a: activity){
                if(a.getRight().isAfter(from)&&a.getRight().isBefore(to))
                    filter.add(a);
            }
            for(UserFriendshipDTO u: friendsActivity)
                if(u.getDate().isAfter(from)&&u.getDate().isBefore(to))
                    filterFriends.add(u);
            for(ConversationDTO c: messagesActivity)
                if(c.getDate().isAfter(from)&&c.getDate().isBefore(to))
                    filterMessages.add(c);
            activity.setAll(filter);
            friendsActivity.setAll(filterFriends);
            messagesActivity.setAll(filterMessages);
        }
        //listView.scrollTo(activity.size()-1);
        tableFriends.scrollTo(friendsActivity.size()-1);
        tableMessages.scrollTo(messagesActivity.size()-1);
    }

    public void handlePDF() {
        if(friendsActivity.isEmpty()&&messagesActivity.isEmpty()){
            MessageAlert.showErrorMessage(null,"There's nothing to export!");
            return;
        }
        try {
            Document document = new Document();
            final FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showOpenDialog(new Stage());
            PdfWriter writer=null;
            if (file != null) {
                writer=PdfWriter.getInstance(document,new FileOutputStream(file));
            }
            //PdfWriter.getInstance(document, new FileOutputStream("activity.pdf"));
            document.open();
            Font f=new Font(Font.FontFamily.TIMES_ROMAN,30.0f,Font.BOLD,BaseColor.BLACK);
            Paragraph p;
            if(datepickerFrom.getValue()==null||datepickerTo.getValue()==null)
                p=new Paragraph("Your overall activity",f);
            else
                p=new Paragraph("Your activity ("+datepickerFrom.getValue()+" - "+datepickerTo.getValue(),f);
            p.setAlignment(Element.ALIGN_CENTER);
            document.add(p);
            f=new Font(Font.FontFamily.TIMES_ROMAN,20.0f,Font.NORMAL,BaseColor.BLACK);
            p=new Paragraph("Your messages\n\n",f);
            p.setAlignment(Element.ALIGN_CENTER);
            document.add(p);
            PdfPTable table=new PdfPTable(5);
            table.addCell(new PdfPCell(new Phrase("From")));
            table.addCell(new PdfPCell(new Phrase("To")));
            table.addCell(new PdfPCell(new Phrase("Message")));
            table.addCell(new PdfPCell(new Phrase("Date")));
            table.addCell(new PdfPCell(new Phrase("Reply to")));
            for(ConversationDTO c:messagesActivity){
                table.addCell(new PdfPCell(new Phrase(c.getFrom())));
                table.addCell(new PdfPCell(new Phrase(c.getTo())));
                table.addCell(new PdfPCell(new Phrase(c.getMessage())));
                table.addCell(new PdfPCell(new Phrase(c.getDate().toString())));
                table.addCell(new PdfPCell(new Phrase(c.getReply())));
            }
            document.add(table);
            p=new Paragraph("\n\nYour friends\n\n",f);
            p.setAlignment(Element.ALIGN_CENTER);
            document.add(p);
            table=new PdfPTable(4);
            table.addCell(new PdfPCell(new Phrase("First name")));
            table.addCell(new PdfPCell(new Phrase("Last name")));
            table.addCell(new PdfPCell(new Phrase("Email")));
            table.addCell(new PdfPCell(new Phrase("Date")));
            for(UserFriendshipDTO uf:friendsActivity){
                table.addCell(new PdfPCell(new Phrase(uf.getFname())));
                table.addCell(new PdfPCell(new Phrase(uf.getLname())));
                table.addCell(new PdfPCell(new Phrase(uf.getEmail())));
                table.addCell(new PdfPCell(new Phrase(uf.getDate().toString())));
            }
            document.add(table);

            JFreeChart barChart = ChartFactory.createBarChart(
                    "Your activity",
                    "Days",
                    "Nr of messages + new friends",
                    createDataset(),
                    PlotOrientation.VERTICAL,
                    true, true, false);
            PdfContentByte pdfContentByte = writer.getDirectContent();
            PdfTemplate pdfTemplate = pdfContentByte.createTemplate(400, 300);
            Graphics2D graphics2d = pdfTemplate.createGraphics(400, 300, new DefaultFontMapper());
            java.awt.geom.Rectangle2D rectangle2d = new java.awt.geom.Rectangle2D.Double(
                    0, 0, 400, 300);
            barChart.draw(graphics2d, rectangle2d);
            graphics2d.dispose();
            pdfContentByte.addTemplate(pdfTemplate, 40, 20);

            document.close();
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Saved!","Your messages were saved in a pdf");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private CategoryDataset createDataset() {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        Map<LocalDate,Long> msgs=new HashMap<>();
        for(ConversationDTO c:messagesActivity){
            msgs.compute(c.getDate(),(k,val)->{if(val==null) return Long.valueOf(1);else return val+1;});
        }
        for(UserFriendshipDTO uf:friendsActivity){
            msgs.compute(uf.getDate(),(k,val)->{if(val==null) return Long.valueOf(1);else return val+1;});
        }
        for(LocalDate l: msgs.keySet()){
            dataset.addValue( msgs.get(l) , l.toString() , "activity" );
        }
        return dataset;
    }

    public void setFields(UserService usrv, FriendshipService fsrv, MessageService msrv, EventService esrv, User logged, Stage primaryStage) {
        this.logged=logged;
        this.usrv=usrv;
        this.fsrv=fsrv;
        this.msrv=msrv;
        this.esrv=esrv;
        this.primaryStage=primaryStage;
        initActivity();
        tableFriends.scrollTo(friendsActivity.size()-1);
        tableMessages.scrollTo(messagesActivity.size()-1);
        tableFriends.setPlaceholder(new Label("No new friends in this period"));
        tableMessages.setPlaceholder(new Label("No new messages in this period"));
        //listView.scrollTo(activity.size()-1);
        //listView.setPlaceholder(new Label("You have no activity so far"));
    }

    private void initActivity() {
        activity.clear();
        messagesActivity.clear();
        friendsActivity.clear();
        for(Message m:msrv.getSome(logged,"from")){
            User to = usrv.getOne(m.getTo().get(0).getId());
            if (m.getReply() == null) {
                activity.add(new Tuple<>("You sent a message to " + to.getFirstName() + " " + to.getLastName() + ": " + m.getMessage(), m.getDate().toLocalDate()));
                ConversationDTO c=new ConversationDTO(logged.getFirstName()+" "+logged.getLastName(),to.getFirstName()+" "+to.getLastName(),m.getMessage(),m.getDate().toLocalDate(),null);
                messagesActivity.add(c);
            }
            else {
                activity.add(new Tuple<>("You replied to " + to.getFirstName() + " " + to.getLastName() + "'s message '" + m.getReply().getMessage() + "' with: " + m.getMessage(), m.getDate().toLocalDate()));
                ConversationDTO c=new ConversationDTO(logged.getFirstName()+" "+logged.getLastName(),to.getFirstName()+" "+to.getLastName(),m.getMessage(),m.getDate().toLocalDate(),m.getReply().getMessage());
                messagesActivity.add(c);
            }
        }
        for(Message m:msrv.getSome(logged,"to")) {
            User from = usrv.getOne(m.getFrom().getId());
            if (m.getReply() == null) {
                activity.add(new Tuple<>("You received a message from " + from.getFirstName() + " " + from.getLastName() + ": " + m.getMessage(), m.getDate().toLocalDate()));
                ConversationDTO c=new ConversationDTO(from.getFirstName()+" "+from.getLastName(),logged.getFirstName()+" "+logged.getLastName(),m.getMessage(),m.getDate().toLocalDate(),null);
                messagesActivity.add(c);
            }
            else {
                activity.add(new Tuple<>(from.getFirstName() + " " + from.getLastName() + " replied to your message '" + m.getReply().getMessage() + "' with: " + m.getMessage(), m.getDate().toLocalDate()));
                ConversationDTO c=new ConversationDTO(from.getFirstName()+" "+from.getLastName(),logged.getFirstName()+" "+logged.getLastName(),m.getMessage(),m.getDate().toLocalDate(),m.getReply().getMessage());
                messagesActivity.add(c);
            }
        }
        for(Friendship f:fsrv.getSome(logged,"All")){
            if(f.getId().getLeft().equals(logged.getId())) {
                User u=usrv.getOne(f.getId().getRight());
                activity.add(new Tuple<String,LocalDate>("You became friends with " +u.getFirstName()+" "+u.getLastName()+" ("+u.getEmail()+")",f.getDate().toLocalDate()));
                UserFriendshipDTO uf=new UserFriendshipDTO(u.getFirstName(),u.getLastName(),u.getEmail(),f.getDate().toLocalDate(),f.getStatus());
                friendsActivity.add(uf);
            }
            if(f.getId().getRight().equals(logged.getId())) {
                User u=usrv.getOne(f.getId().getLeft());
                activity.add(new Tuple<String,LocalDate>("You became friends with " +u.getFirstName()+" "+u.getLastName()+" ("+u.getEmail()+")",f.getDate().toLocalDate()));
                UserFriendshipDTO uf=new UserFriendshipDTO(u.getFirstName(),u.getLastName(),u.getEmail(),f.getDate().toLocalDate(),f.getStatus());
                friendsActivity.add(uf);
            }
        }
        Comparator<Tuple<String,LocalDate>> byDate = new Comparator<Tuple<String,LocalDate>>() {
            public int compare(Tuple<String,LocalDate> m1, Tuple<String,LocalDate> m2) {
                return Integer.valueOf(m1.getRight().compareTo(m2.getRight()));
            }
        };
        Collections.sort(activity,byDate);
    }
}
