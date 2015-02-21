package org.obehave.persistence;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obehave.model.Action;
import org.obehave.model.Coding;
import org.obehave.model.Color;
import org.obehave.model.Subject;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

/**
 * Created by Markus.Moeslinger on 17.02.2015.
 */
public class CodingDaoTest extends DaoTestBase {
    private static CodingDao dao;
    private static SubjectDao subjectDao;
    private Coding coding;
    private Subject subject;
    private Action action;

    @BeforeClass
    public static void prepare() throws SQLException {
        dao = Daos.coding();
        subjectDao = Daos.subject();
    }

    @Before
    public void prepareData() {
        subject = new Subject("Name1");
        subject.setColor(new Color(255, 240, 230, 220));

        action = new Action("Playing");

        coding = new Coding(subject, action, 10);
    }

    @Test
    public void readAndWrite() throws SQLException {
        subjectDao.create(subject);
        dao.create(coding);

        Coding loadedCoding = dao.queryForSameId(coding);
        assertEquals(subject, loadedCoding.getSubject());
        assertEquals(action, loadedCoding.getAction());
    }

    @Test
    public void persistingEnumerationModifiedCoding() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Test
    public void persistingSubjectModifiedCoding() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Test
    public void persistingNumberModifiedCoding() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}