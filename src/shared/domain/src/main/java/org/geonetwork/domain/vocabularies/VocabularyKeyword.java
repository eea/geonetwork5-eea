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

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Table(name = VOC_PREFIX + "contains_keywords")
@SuppressWarnings("unused")
public class VocabularyKeyword {
    @EmbeddedId
    private VocabularyKeywordKey id;

    @ManyToOne
    private Vocabulary vocabulary;

    @ManyToOne
    private Keyword keyword;

    //    @SuppressWarnings("unread")
    //    boolean isVirtual;
}
