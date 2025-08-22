package com.self.userauth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.self.userauth.model.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FilterDef(name = "activeUserFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class))
@Filter(name = "activeUserFilter", condition = "deleted_at IS NULL")
public class User extends BaseEntity {

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@Column(name = "profile_image")
	private String profileImage;

	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Phones phone;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<Emails> emails;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JsonIgnore  // Prevent returning refresh tokens in JSON while fetching from db
	private List<RefreshTokens> refreshTokens;

	@Override
	protected String getPrefix() {
		return "users";
	}
	
	// Add helper method in User entity
	public void addRefreshToken(RefreshTokens token) {
	    if (this.refreshTokens == null) {
	        this.refreshTokens = new ArrayList<>();
	    }
	    this.refreshTokens.add(token);
	    token.setUser(this);
	}

}
