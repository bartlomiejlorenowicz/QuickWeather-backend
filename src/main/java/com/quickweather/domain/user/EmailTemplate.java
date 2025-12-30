package com.quickweather.domain.user;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "email_templates")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_code", nullable = false, unique = true)
    private String templateCode;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "body", nullable = false, columnDefinition = "text")
    private String body;
}
