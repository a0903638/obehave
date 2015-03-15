package org.obehave.view.controller.components.edit;

import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import org.obehave.model.Node;
import org.obehave.model.Subject;
import org.obehave.service.NodeService;
import org.obehave.service.SubjectService;
import org.obehave.view.util.ColorConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Markus Möslinger
 */
public class SubjectEditController {
    private static final Logger log = LoggerFactory.getLogger(SubjectEditController.class);
    private static final SubjectService subjectService = SubjectService.getInstance();
    private static final NodeService nodeService = NodeService.getInstance();

    private Node loadedSubjectNode;
    private Runnable saveCallback;

    @FXML
    private TextField name;

    @FXML
    private TextField alias;

    @FXML
    private ColorPicker colorPicker;

    public String getName() {
        return name.getText();
    }

    public void setName(String name) {
        this.name.setText(name);
    }

    public String getAlias() {
        return alias.getText();
    }

    public void setAlias(String alias) {
        this.alias.setText(alias);
    }

    public ColorPicker getColorPicker() {
        return colorPicker;
    }

    public void setColorPicker(ColorPicker colorPicker) {
        this.colorPicker = colorPicker;
    }

    public void loadSubject(Node node) {
        loadedSubjectNode = node;
        Subject s = (Subject) node.getData();

        if (s != null) {
            setName(s.getName());
            setAlias(s.getAlias());
            colorPicker.setValue(ColorConverter.convertToJavaFX(s.getColor()));
        } else {
            setName("");
            setAlias("");
            colorPicker.setValue(Color.BLACK);
        }
    }

    public void saveCurrent() {
        Subject subject;

        if (loadedSubjectNode.getData() == null) {
            log.debug("Creating new subject");
            subject = new Subject();
            loadedSubjectNode.addChild(subject);
        } else {
            subject = (Subject) loadedSubjectNode.getData();
        }

        subject.setName(getName());
        subject.setAlias(getAlias());
        subject.setColor(ColorConverter.convertToObehave(colorPicker.getValue()));

        subjectService.save(subject);
        nodeService.save(loadedSubjectNode);

        loadedSubjectNode = null;
        saveCallback.run();
    }

    public void cancel() {
        saveCallback.run();
    }

    public void setSaveCallback(Runnable saveCallback) {
        this.saveCallback = saveCallback;
    }
}
