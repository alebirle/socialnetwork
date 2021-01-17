package controller;

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import socialnetwork.domain.Message;
import socialnetwork.domain.MessageDTO;
import socialnetwork.domain.User;
import socialnetwork.domain.UserFriendshipDTO;
import socialnetwork.service.EventService;
import socialnetwork.service.FriendshipService;
import socialnetwork.service.MessageService;
import socialnetwork.service.UserService;
import javafx.scene.control.TextField;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

public class MessagesController {
    //public javafx.scene.control.TextField textMsg;
    @FXML
    ListView<VBox> listView;
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
    ObservableList<VBox> listModel=FXCollections.observableArrayList();
    Map<VBox,MessageDTO> map=new HashMap<>();
    private EventService esrv;
    private int currentPage;

    public void setFields(UserService usrv, FriendshipService fsrv, MessageService msrv, EventService esrv, User logged, User conv, Stage dialogStage) {
        this.usrv=usrv;
        this.fsrv=fsrv;
        this.msrv=msrv;
        this.esrv=esrv;
        this.logged=logged;
        this.conv=conv;
        this.dialogStage=dialogStage;
        currentPage=0;
        initModel(true);
        listView.scrollTo(listView.getItems().size()-1);
        textMsg.setFocusTraversable(false);
        listView.setPlaceholder(new Label("You have no messages"));
        /*ScrollBar listViewScrollBar = getListViewScrollBar(listView);
        listViewScrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
            double position = newValue.doubleValue();
            ScrollBar scrollBar = getListViewScrollBar(listView);
            if (position == scrollBar.getMax()) {
                currentPage+=1;
                initModel();
            }
        });*/
    }

    private void initModel(boolean ok) {
        List<MessageDTO> msgs=new ArrayList<>();
        Iterable<Message> messages;
        if(ok==true)
            messages=msrv.getSome(new User(logged.getId(), String.valueOf(conv.getId())), String.valueOf(6+currentPage));
        else
            messages= msrv.getAll();
        for(Message m: messages){
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
        listModel.clear();
        for(MessageDTO m:msgs){
            TextArea textArea;
            if(m.getReply()==null)
                textArea=new TextArea(m.getMessage());
            else
                textArea=new TextArea("Reply to '"+m.getReply()+"': "+m.getMessage());
            textArea.setFocusTraversable(false);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefWidth(200);
            int l=(int)Math.floor(textArea.getText().length()/35)+1;
            textArea.setPrefHeight(30*l);
            Polygon polygon=new Polygon();
            GridPane gridPane=new GridPane();
            Label label=new Label(m.getDate().toLocalDate().toString());
            gridPane.setPrefWidth(220);
            if(m.getEmail().equals(logged.getEmail())) {
                polygon.getPoints().addAll(new Double[]{
                        0.0, 0.0,
                        20.0, 10.0,
                        0.0, 20.0});
                gridPane.add(textArea,0,0);
                gridPane.add(polygon,1,0);
                gridPane.setPadding(new Insets(0,0,0,520));
                label.setPadding(new Insets(0,0,0,520));
                polygon.setFill(Color.web("#61a79f"));
                textArea.setStyle("-fx-control-inner-background: #61a79f");
            }
            else{
                polygon.getPoints().addAll(new Double[]{
                        20.0, 0.0,
                        0.0, 10.0,
                        20.0, 20.0});
                gridPane.add(textArea,1,0);
                gridPane.add(polygon,0,0);
                gridPane.setPadding(new Insets(0,350,0,0));
                label.setPadding(new Insets(0,0,0,20));
                polygon.setFill(Color.web("#c3d9d6"));
                textArea.setStyle("-fx-control-inner-background: #c3d9d6");
            }
            VBox vBox=new VBox(gridPane,label);
            listModel.add(vBox);
            map.putIfAbsent(vBox,m);
        }
        //listView.scrollTo(listView.getItems().size()-1);
    }

    @FXML
    public void initialize() {
        datepickerTo.setFocusTraversable(false);
        datepickerFrom.setFocusTraversable(false);
        listView.setItems(listModel);
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
        if(listModel.isEmpty()){
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
                writer = PdfWriter.getInstance(document,new FileOutputStream(file));
            }
            //PdfWriter.getInstance(document, new FileOutputStream("messages.pdf"));
            document.open();
            PdfPTable table = new PdfPTable(5);
            Font f=new Font(Font.FontFamily.TIMES_ROMAN,30.0f,Font.BOLD,BaseColor.BLACK);
            Paragraph p;
            if(datepickerFrom.getValue()==null||datepickerTo.getValue()==null)
                p=new Paragraph("\n\n\n\n\n\n\nAll your messages with "+conv.getFirstName()+" "+conv.getLastName()+"\n\n",f);
            else
                p=new Paragraph("\n\n\n\n\n\n\nYour messages with "+conv.getFirstName()+" "+conv.getLastName()+" ("+datepickerFrom.getValue()+" - "+datepickerTo.getValue()+"\n\n",f);
            p.setAlignment(Element.ALIGN_CENTER);
            //document.add(p);
            table.addCell(new PdfPCell(new Phrase("From")));
            table.addCell(new PdfPCell(new Phrase("To")));
            table.addCell(new PdfPCell(new Phrase("Message")));
            table.addCell(new PdfPCell(new Phrase("Date")));
            table.addCell(new PdfPCell(new Phrase("Reply to")));
            if(datepickerFrom.getValue()==null&&datepickerTo.getValue()==null)
                initModel(false);
            for(VBox v:listModel){
                MessageDTO m=map.get(v);
                table.addCell(new PdfPCell(new Phrase(m.getFname()+" "+m.getLname())));
                if(m.getEmail().equals(logged.getEmail()))
                    table.addCell(new PdfPCell(new Phrase(conv.getFirstName()+" "+conv.getLastName())));
                else
                    table.addCell(new PdfPCell(new Phrase(logged.getFirstName()+" "+logged.getLastName())));
                table.addCell(new PdfPCell(new Phrase(m.getMessage())));
                table.addCell(new PdfPCell(new Phrase(m.getDate().toLocalDate().toString())));
                table.addCell(new PdfPCell(new Phrase(m.getReply())));
            }
            //document.add(table);
            JFreeChart barChart = ChartFactory.createBarChart(
                    "Your messages",
                    "Days",
                    "Number of messages",
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
            pdfContentByte.addTemplate(pdfTemplate, 40, 500);
            document.add(p);
            document.add(table);
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
        for(VBox v:listModel){
            MessageDTO m=map.get(v);
            msgs.compute(m.getDate().toLocalDate(),(k,val)->{if(val==null) return Long.valueOf(1);else return val+1;});
        }
        for(LocalDate l: msgs.keySet()){
            dataset.addValue( msgs.get(l) , l.toString() , "messages" );
        }
        return dataset;
    }

    public void handleShow() {
        if(datepickerFrom.getValue()==null||datepickerTo.getValue()==null){
            MessageAlert.showErrorMessage(null,"You must pick an interval!");
        }
        else{
            LocalDate from=datepickerFrom.getValue();
            LocalDate to=datepickerTo.getValue();
            List<VBox> filter=new ArrayList<>();
            initModel(false);
            for(VBox v:listModel){
                MessageDTO m=map.get(v);
                if(m.getDate().toLocalDate().isAfter(from)&&m.getDate().toLocalDate().isBefore(to))
                    filter.add(v);
            }
            listModel.setAll(filter);
            listView.scrollTo(listView.getItems().size()-1);
        }
    }

    public void handleSend() {
        if(datepickerTo.getValue()!=null)
            datepickerTo.setValue(null);
        if(datepickerFrom.getValue()!=null)
            datepickerFrom.setValue(null);
        if(!textMsg.getText().isEmpty()){
            Message m = new Message(logged, Collections.singletonList(conv), textMsg.getText());
            if(listView.getSelectionModel().getSelectedItem()!=null){
                Message reply=msrv.getOne(map.get(listView.getSelectionModel().getSelectedItem()).getId());
                m.setReply(reply);
            }
            msrv.save(m);
            initModel(true);
            textMsg.clear();
        }
    }

    private ScrollBar getListViewScrollBar(ListView<?> listView) {
        ScrollBar scrollbar = null;
        for (Node node : listView.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar) {
                ScrollBar bar = (ScrollBar) node;
                if (bar.getOrientation().equals(Orientation.VERTICAL)) {
                    scrollbar = bar;
                }
            }
        }
        return scrollbar;
    }

    public void handleScroll(ScrollEvent scrollEvent) {
        if(datepickerFrom.getValue()==null&&datepickerTo.getValue()==null) {
            currentPage += 1;
            if(currentPage+6<msrv.getNr())
                initModel(true);
        }
    }
}
