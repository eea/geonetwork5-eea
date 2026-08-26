/*
 * SPDX-FileCopyrightText: 2001 FAO-UN and others <geonetwork@osgeo.org>
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
/*
 * (c) 2003 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license,
 * available at the root application directory.
 */
package org.geonetwork.domain.vocabularies;

import static org.geonetwork.domain.vocabularies.Constants.VOC_PREFIX;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = VOC_PREFIX + "vocabularies")
public class Vocabulary {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = VOC_PREFIX + "vocabularies_id_gen")
    @SequenceGenerator(
            name = VOC_PREFIX + "vocabularies_id_gen",
            sequenceName = VOC_PREFIX + "vocabularies_id_seq",
            allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Min(value = 3, message = "Name should not be less than 3")
    @Max(value = 255, message = "Name should not be greater than 255")
    private String name;

    @NotNull(message = "Vocabulary source URL cannot be null")
    @Column(nullable = false, length = Integer.MAX_VALUE)
    private String sourceUrl;
}
