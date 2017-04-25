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
@Table(name = "registration")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@NamedQueries({
        @NamedQuery(name = "Registration.findAll", query = "SELECT a FROM Registration a"),
        @NamedQuery(name = "Registration.findByGuid", query = "SELECT a FROM Registration a WHERE a.guid = :guid"),
        @NamedQuery(name = "Registration.findByUserId", query = "SELECT a FROM Registration a WHERE a.userId = :userId"),
        @NamedQuery(name = "Registration.findBySection", query = "SELECT a FROM Registration a WHERE a.courseSection = :courseSection")})
public class Registration implements Serializable {

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
    @NotNull
    @Size(max = 250)
    @Column(name = "user_id")
    private String userId;

    @Expose()
    @NotNull
    @JoinColumn(name = "course_section_guid", referencedColumnName = "guid")
    @ManyToOne
    private CourseSection courseSection;

    @Expose()
    @NotNull
    @Column(name = "role")
    private String scope;

    @Expose()
    @Column(name = "date_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Column(name = "date_updated")
    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    private Date dateUpdated;


    public Registration() {
        this.dateCreated = new Date();
        this.dateUpdated = (Date) dateCreated.clone();
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public CourseSection getCourseSection() {
        return courseSection;
    }

    public void setCourseSection(CourseSection courseSection) {
        this.courseSection = courseSection;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String role) {
        this.scope = role;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
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

        Registration that = (Registration) o;

        if (guid != null ? !guid.equals(that.guid) : that.guid != null) return false;
        if (!userId.equals(that.userId)) return false;
        if (!courseSection.equals(that.courseSection)) return false;
        return scope == that.scope;
    }

    @Override
    public int hashCode() {
        int result = guid != null ? guid.hashCode() : 0;
        result = 31 * result + userId.hashCode();
        result = 31 * result + courseSection.hashCode();
        result = 31 * result + scope.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Registration{" +
                "userId='" + userId + '\'' +
                ", courseSection=" + courseSection +
                ", role=" + scope +
                '}';
    }
}
