package org.obehave.service;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import org.obehave.events.EventBusHolder;
import org.obehave.events.RepaintStudyEvent;
import org.obehave.exceptions.Validate;
import org.obehave.model.*;
import org.obehave.model.modifier.ModifierFactory;
import org.obehave.persistence.Daos;
import org.obehave.util.DatabaseProperties;
import org.obehave.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;

/**
 * A study contains multiple subjects, actions and observations.
 */
public class Study implements Displayable {
    private static final Logger log = LoggerFactory.getLogger(Study.class);

    private String name;

    private Node subjects = new Node(Subject.class);
    private Node actions = new Node(Action.class);
    private Node observations = new Node(Observation.class);
    private Node modifierFactories = new Node(ModifierFactory.class);

    private SuggestionService suggestionService;
    private ActionService actionService;
    private NodeService nodeService;
    private ModifierFactoryService modifierFactoryService;
    private ObservationService observationService;
    private SubjectService subjectService;

    private File savePath;

    private Study() {

    }

    private Study(File savePath) {
        setSavePath(savePath);
    }

    @Deprecated
    public Study(String name) {
        this.name = name;
    }

    @Deprecated
    public static Study create() {
        log.info("Creating empty study");

        return new Study();
    }

    public static Study create(File savePath) throws SQLException {
        log.info("Creating new study at {}", savePath);

        if (savePath.exists()) {
            log.info("File {} exists already, creating new one at same path", savePath);
            if (!savePath.delete()) {
                log.error("Couldn't delete file {}!" + savePath);
            }
        }

        final Study study = new Study(savePath);
        Daos.asDefault(new JdbcConnectionSource(Properties.getDatabaseConnectionStringWithInit(savePath)));

        study.subjects = Validate.hasOnlyOneElement(Daos.get().node().getRoot(Subject.class)).get(0);
        study.actions = Validate.hasOnlyOneElement(Daos.get().node().getRoot(Action.class)).get(0);
        study.modifierFactories = Validate.hasOnlyOneElement(Daos.get().node().getRoot(ModifierFactory.class)).get(0);
        study.observations = Validate.hasOnlyOneElement(Daos.get().node().getRoot(Observation.class)).get(0);

        return study;
    }

    public static Study load(File savePath) throws SQLException {
        log.info("Loading existing study from {}", savePath);

        final Study study = new Study(savePath);
        Daos.asDefault(new JdbcConnectionSource(Properties.getDatabaseConnectionString(savePath)));
        study.load();
        return study;
    }

    private void load() throws SQLException {
        log.info("Starting loading of entities");
        long startLoad = System.currentTimeMillis();

        // we want to load a single value first to establish a database connection
        name = DatabaseProperties.get(DatabaseProperties.STUDY_NAME);
        final long startEntities = System.currentTimeMillis();

        subjects = Validate.hasOnlyOneElement(Daos.get().node().getRoot(Subject.class)).get(0);
        actions = Validate.hasOnlyOneElement(Daos.get().node().getRoot(Action.class)).get(0);
        modifierFactories = Validate.hasOnlyOneElement(Daos.get().node().getRoot(ModifierFactory.class)).get(0);
        observations = Validate.hasOnlyOneElement(Daos.get().node().getRoot(Observation.class)).get(0);

        removeEmptyNodes(subjects);
        removeEmptyNodes(actions);
        removeEmptyNodes(modifierFactories);
        removeEmptyNodes(observations);

        final long durationEntities = System.currentTimeMillis() - startEntities;
        final long durationLoad = System.currentTimeMillis() - startLoad;
        log.info("Took {}ms for loading of entities ({}ms in total for loading)", durationEntities, durationLoad);
    }

    private void removeEmptyNodes(Node node) {
        if (node.getData() == null && (node.getTitle() == null || node.getTitle().isEmpty())) {
            node.getParent().remove(node);
            log.warn("Why is there an empty node at all? Look! " + node);
        } else {
            for (Node child : node.getChildren()) {
                removeEmptyNodes(child);
            }
        }
    }

    public Node getSubjects() {
        return subjects;
    }

    public Node getActions() {
        return actions;
    }

    public Node getObservations() {
        return observations;
    }

    public Node getModifierFactories() {
        return modifierFactories;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        log.debug("Setting study name to {}", name);
        this.name = name;
        DatabaseProperties.set(DatabaseProperties.STUDY_NAME, name);

        EventBusHolder.post(new RepaintStudyEvent());
    }

    public File getSavePath() {
        return savePath;
    }

    public void setSavePath(File savePath) {
        this.savePath = savePath;
    }

    @Override
    public String getDisplayString() {
        return getName();
    }

    public SuggestionService getSuggestionService() {
        if (suggestionService == null) {
            suggestionService = new SuggestionService(this);
        }

        return suggestionService;
    }

    public ActionService getActionService() {
        if (actionService == null) {
            actionService = new ActionService(this);
        }

        return actionService;
    }

    public ObservationService getObservationService() {
        if (observationService == null) {
            observationService = new ObservationService(this);
        }

        return observationService;
    }

    public ModifierFactoryService getModifierFactoryService() {
        if (modifierFactoryService == null) {
            modifierFactoryService = new ModifierFactoryService(this);
        }

        return modifierFactoryService;
    }

    public NodeService getNodeService() {
        if (nodeService == null) {
            nodeService = new NodeService(this);
        }

        return nodeService;
    }

    public SubjectService getSubjectService() {
        if (subjectService == null) {
            subjectService = new SubjectService(this);
        }

        return subjectService;
    }
}
