package edu.cmu.oli.secure.domain;

import com.google.gson.annotations.Expose;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Raphael Gachuhi
 */
@Entity
@Table(name = "learning_activity")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@NamedQueries({
        @NamedQuery(name = "LearningActivity.findAll", query = "SELECT a FROM LearningActivity a"),
        @NamedQuery(name = "LearningActivity.findByGuid", query = "SELECT a FROM LearningActivity a WHERE a.guid = :guid"),
        @NamedQuery(name = "LearningActivity.findByName", query = "SELECT a FROM LearningActivity a WHERE a.id = :id"),
        @NamedQuery(name = "LearningActivity.findByDescription", query = "SELECT a FROM LearningActivity a WHERE a.description = :description"),
        @NamedQuery(name = "LearningActivity.findByCreated", query = "SELECT a FROM LearningActivity a WHERE a.created = :created")})
public class LearningActivity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Expose()
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(max = 32)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @Column(name = "guid")
    private String guid;

    @Expose()
    @Size(max = 50)
    @Column(name = "type")
    private String type;

    @Expose()
    @Size(max = 255)
    @Column(name = "title")
    private String title;

    @Expose()
    @NotNull
    @Size(max = 250)
    @Column(name = "id")
    private String id;

    @Expose()
    @Size(max = 250)
    @Column(name = "description")
    private String description;

    @Expose()
    @Size(max = 250)
    @Column(name = "author")
    private String author;

    @Expose()
    @Column(name = "published")
    private boolean published;

    @Expose()
    @Column(name = "date_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Column(name = "date_updated")
    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    private Date dateUpdated;

    @Expose()
    @JoinColumn(name = "course_section_guid", referencedColumnName = "guid")
    @ManyToOne
    private CourseSection courseSection;

    public LearningActivity() {
        this.dateCreated = new Date();
        this.dateUpdated = (Date) dateCreated.clone();
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LearningActivity learningActivity = (LearningActivity) o;

        if (!guid.equals(learningActivity.guid)) return false;
        return id.equals(learningActivity.id);
    }

    @Override
    public int hashCode() {
        int result = guid.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "LearningActivity[ guid=" + guid + " ]";
    }

}
