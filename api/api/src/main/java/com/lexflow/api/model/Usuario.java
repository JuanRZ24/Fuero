package com.lexflow.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

@org.hibernate.annotations.SQLDelete(sql = "UPDATE usuarios SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@org.hibernate.annotations.SQLRestriction("deleted_at IS NULL")
public class Usuario implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolUsuario rol;

    @Column(name = "google_refresh_token")
    private String googleRefreshToken;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Le pasamos el rol a Spring Security para que sepa si es Admin, Abogado, etc.
        return List.of(new SimpleGrantedAuthority(rol.name()));
    }

    @Override
    public String getPassword() {
        return this.passwordHash; // O el nombre que le hayas puesto a tu campo de contraseña
    }

    @Override
    public String getUsername() {
        return this.email; // En LexFlow, el email es el usuario
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.activo; // Tu campo booleano que dice si el usuario está activo
    }
}
