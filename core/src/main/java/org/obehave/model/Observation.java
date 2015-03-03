package org.obehave.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;
import org.obehave.exceptions.Validate;
import org.obehave.persistence.impl.ObservationDaoImpl;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * During an observation, it's possible to code subjects and actions.
 */
@DatabaseTable(tableName = "Observation", daoClass = ObservationDaoImpl.class)
public class Observation extends BaseEntity implements Displayable, Serializable {
    private static final long serialVersionUID = 1L;
    public static final String COLUMN_NAME = "name";

    @DatabaseField(columnName = COLUMN_NAME)
    private String name;

    @DatabaseField(columnName = "video")
    private File video;

    @DatabaseField(columnName = "date")
    private DateTime dateTime;

    @ForeignCollectionField
    private Collection<Coding> codings = new ArrayList<>();

    private Observation() {
        // for frameworks
    }

    public Observation(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null!");
        }
        this.name = name;
    }

    public File getVideo() {
        return video;
    }

    public void setVideo(File video) {
        this.video = video;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        if (dateTime == null) {
            throw new IllegalArgumentException("dateTime must not be null!");
        }

        this.dateTime = dateTime;
    }

    public void addCoding(Coding coding) {
        Validate.isNotNull(coding, "Coding");

        codings.add(coding);
        coding.setObservation(this);
    }

    public List<Coding> getCodings() {
        return Collections.unmodifiableList(new ArrayList<Coding>(codings));
    }

    @Override
    public String getDisplayString() {
        return getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        Observation rhs = (Observation) obj;

        return new EqualsBuilder().append(name, rhs.name).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).build();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append(super.toString()).append("name", name).append("dateTime", dateTime).append("video", video).toString();
    }
}
