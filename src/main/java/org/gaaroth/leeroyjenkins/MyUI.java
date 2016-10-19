package org.gaaroth.leeroyjenkins;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.BeanBinder;
import com.vaadin.data.util.converter.StringToLongConverter;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * This UI is the application entry point. A UI may either represent a browser window 
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be 
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@SuppressWarnings("serial")
@Theme("mytheme")
public class MyUI extends UI {

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        VerticalLayout layoutRoot = new VerticalLayout();
        
        // TABLE
        Label titleGrid = new Label("BOOK GRID");
        titleGrid.addStyleName(ValoTheme.LABEL_COLORED);
        titleGrid.addStyleName(ValoTheme.LABEL_H2);
        
        Grid<Book> grid = new Grid<>();
		grid.setSizeFull();
		grid.addColumn("ID", book -> book.getId().toString());
		grid.addColumn("Title", book -> book.getTitle());
		grid.addColumn("Description", book -> book.getDescription());

        Button reload = new Button("Load new data");
        reload.addClickListener(event -> grid.setItems(generateRandomBooks()));
        
        //FORM
        Label titleForm = new Label("BOOK FORM");
        titleForm.addStyleName(ValoTheme.LABEL_COLORED);
        titleForm.addStyleName(ValoTheme.LABEL_H2);
        
        TextField idField = new TextField();
        idField.setEnabled(false);
        TextField titleField = new TextField();
        TextField descriptionField = new TextField();
        Button refresh = new Button("Refresh grid");
        
        final BeanBinder<Book> binder = new BeanBinder<>(Book.class);
        binder.forField(idField).withConverter(new StringToLongConverter("Not a valid ID")).bind("id");
        binder.forField(titleField).withValidator(value -> value.length() > 0, "Must be compiled").bind("title");
        binder.forField(descriptionField).bind("description");
        binder.setValidationStatusHandler(validationStatus -> refresh.setEnabled(validationStatus.isOk()));
     
        titleField.addValueChangeListener(event -> binder.validate());
        grid.addItemClickListener(item -> binder.bind(item.getItem()));
        refresh.addClickListener(event -> {
        	grid.getDataSource().refreshAll();
        });
        
        final HorizontalLayout layoutOverTable = new HorizontalLayout();
        layoutOverTable.setWidth("100%");
        layoutOverTable.addComponents(titleGrid, reload);
        layoutOverTable.setComponentAlignment(reload, Alignment.MIDDLE_RIGHT);
        layoutOverTable.setSpacing(true);
        
        final HorizontalLayout layoutBinder = new HorizontalLayout();
        layoutBinder.setWidth("100%");
        layoutBinder.addComponents(idField, titleField, descriptionField, refresh);
        layoutBinder.setComponentAlignment(refresh, Alignment.MIDDLE_RIGHT);
        layoutBinder.setExpandRatio(refresh, 1);
        layoutBinder.setSpacing(true);
        
        final VerticalLayout layoutForm = new VerticalLayout();
        layoutForm.setWidth("100%");
        layoutForm.addComponents(titleForm, layoutBinder);
        layoutForm.setSpacing(true);
        
        layoutRoot.addComponents(layoutOverTable, grid, layoutForm);
        layoutRoot.setMargin(true);
        layoutRoot.setSpacing(true);
        
        setContent(layoutRoot);

    }
    
    private List<Book> generateRandomBooks() {
    	List<Book> list = new ArrayList<>();
    	for (int i = 0; i < Math.random()*100; i++) {
    		list.add(new Book(new Long(i), "TITLE-" + Math.random(), "DESC-" + Math.random()));
    	}
    	return list;
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
