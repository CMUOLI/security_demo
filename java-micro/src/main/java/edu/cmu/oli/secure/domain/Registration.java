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
    @Size(max = 250)
    @Column(name = "user_id")
    private String userId;

    @Expose()
    @JoinColumn(name = "course_section_guid", referencedColumnName = "guid")
    @ManyToOne
    private CourseSection courseSection;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Roles role;

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

}
