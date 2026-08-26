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
@Table(name = VOC_PREFIX + "keywords")
public class Keyword {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = VOC_PREFIX + "keywords_id_gen")
    @SequenceGenerator(
            name = VOC_PREFIX + "keywords_id_gen",
            sequenceName = VOC_PREFIX + "keywords_id_seq",
            allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    private String label;

    private String icon;

    private int position;
}
