package controller;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import socialnetwork.domain.Message;
import socialnetwork.domain.MessageDTO;
import socialnetwork.domain.User;
import socialnetwork.domain.UserFriendshipDTO;
import socialnetwork.service.EventService;
import socialnetwork.service.FriendshipService;
import socialnetwork.service.MessageService;
import socialnetwork.service.UserService;
import javafx.scene.control.TextField;
import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MessagesController {
    //public javafx.scene.control.TextField textMsg;
    @FXML
    TableView<MessageDTO> tableView;
    @FXML
    TableColumn<MessageDTO,String> tableColumnFName;
    @FXML
    TableColumn<MessageDTO,String> tableColumnLName;
    @FXML
    TableColumn<MessageDTO,String> tableColumnEmail;
    @FXML
    TableColumn<MessageDTO,String> tableColumnMessage;
    @FXML
    TableColumn<MessageDTO,LocalDateTime> tableColumnDate;
    @FXML
    TableColumn<MessageDTO,String> tableColumnReply;
    @FXML
    DatePicker datepickerFrom;
    @FXML
    DatePicker datepickerTo;
    @FXML
    TextField textMsg;
    private UserService usrv;
    private FriendshipService fsrv;
    private MessageService msrv;
    private User logged;
    private User conv;
    private Stage dialogStage;
    ObservableList<MessageDTO> model = FXCollections.observableArrayList();
    private EventService esrv;

    public void setFields(UserService usrv, FriendshipService fsrv, MessageService msrv, EventService esrv, User logged, User conv, Stage dialogStage) {
        this.usrv=usrv;
        this.fsrv=fsrv;
        this.msrv=msrv;
        this.esrv=esrv;
        this.logged=logged;
        this.conv=conv;
        this.dialogStage=dialogStage;
        initModel();
        textMsg.setFocusTraversable(false);
        tableView.setPlaceholder(new Label("You have no messages"));
    }

    private void initModel() {
        List<MessageDTO> msgs=new ArrayList<>();
        for(Message m: msrv.getAll()){
            User to=m.getTo().get(0);
            if(m.getFrom().getId().equals(logged.getId())&&to.getId().equals(conv.getId())){
                String reply;
                if(m.getReply()==null)
                    reply=null;
                else
                    reply=m.getReply().getMessage();
                MessageDTO msg=new MessageDTO(m.getId(),logged.getFirstName(),logged.getLastName(),logged.getEmail(),m.getMessage(),m.getDate(),reply);
                msgs.add(msg);
            }
            if(m.getFrom().getId().equals(conv.getId())&&to.getId().equals(logged.getId())){
                String reply;
                if(m.getReply()==null)
                    reply=null;
                else
                    reply=m.getReply().getMessage();
                MessageDTO msg=new MessageDTO(m.getId(),conv.getFirstName(),conv.getLastName(),conv.getEmail(),m.getMessage(),m.getDate(),reply);
                msgs.add(msg);
            }
        }
        Comparator<MessageDTO> byDate = new Comparator<MessageDTO>() {
            public int compare(MessageDTO m1, MessageDTO m2) {
                return Integer.valueOf(m1.getDate().compareTo(m2.getDate()));
            }
        };
        Collections.sort(msgs, byDate);
        model.setAll(msgs);
    }

    @FXML
    public void initialize() {
        tableColumnFName.setCellValueFactory(new PropertyValueFactory<MessageDTO,String>("fname"));
        tableColumnLName.setCellValueFactory(new PropertyValueFactory<MessageDTO,String>("lname"));
        tableColumnEmail.setCellValueFactory(new PropertyValueFactory<MessageDTO,String>("email"));
        tableColumnMessage.setCellValueFactory(new PropertyValueFactory<MessageDTO,String>("message"));
        tableColumnDate.setCellValueFactory(new PropertyValueFactory<MessageDTO, LocalDateTime>("date"));
        tableColumnReply.setCellValueFactory(new PropertyValueFactory<MessageDTO,String>("reply"));
        tableView.setItems(model);
        tableView.scrollTo(tableView.getItems().size()-1);
        datepickerTo.setFocusTraversable(false);
        datepickerFrom.setFocusTraversable(false);
    }

    public void handleCancel() throws IOException {
        FXMLLoader dbLoader = new FXMLLoader();
        dbLoader.setLocation(getClass().getResource("/views/DashboardView.fxml"));
        AnchorPane dbLayout = dbLoader.load();
        dialogStage.setScene(new Scene(dbLayout));
        dialogStage.setTitle("Your page");
        DashboardController dbController = dbLoader.getController();
        dbController.setFields(logged,usrv,fsrv,msrv,esrv, dialogStage);
    }

    public void handlePDF() {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("messages.pdf"));
            document.open();
            PdfPTable table = new PdfPTable(1);
            Phrase p=new Phrase("Messages");
            p.setFont(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16));
            PdfPCell title=new PdfPCell(p);
            title.setBorder(Rectangle.BOTTOM);
            table.addCell(title);
            for(MessageDTO m:tableView.getItems()) {
                PdfPCell cell=new PdfPCell(new Phrase(m.toString()));
                cell.setBorder(Rectangle.NO_BORDER);
                table.addCell(cell);
            }
            document.add(table);
            document.close();
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Saved!","Your messages were saved in a pdf");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void handleShow() {
        if(datepickerFrom.getValue()==null||datepickerTo.getValue()==null){
            MessageAlert.showErrorMessage(null,"You must pick an interval!");
        }
        else{
            LocalDate from=datepickerFrom.getValue();
            LocalDate to=datepickerTo.getValue();
            List<MessageDTO> filter=new ArrayList<>();
            for(MessageDTO m: model){
                if(m.getDate().toLocalDate().isAfter(from)&&m.getDate().toLocalDate().isBefore(to))
                    filter.add(m);
            }
            model.setAll(filter);
        }
    }

    public void handleSend() {
        if(!textMsg.getText().isEmpty()){
            Message m = new Message(logged, Collections.singletonList(conv), textMsg.getText());
            if(tableView.getSelectionModel().getSelectedItem()!=null) {
                Message reply=msrv.getOne(tableView.getSelectionModel().getSelectedItem().getId());
                m.setReply(reply);
            }
            msrv.save(m);
            initModel();
            textMsg.clear();
        }
    }
}
