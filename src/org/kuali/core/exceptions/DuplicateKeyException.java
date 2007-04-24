/*
 * Copyright 2005-2006 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.core.exceptions;

/**
 * This class represents an exception that is thrown when the configuration service tries to redefine a configuration property with
 * a key which is already in use.
 * 
 * 
 */
public class DuplicateKeyException extends RuntimeException {

    private static final long serialVersionUID = 6111570264943143198L;

    /**
     * @param message
     */
    public DuplicateKeyException(String message) {
        super(message);
    }

    /**
     * @param message
     */
    public DuplicateKeyException(String message, Throwable t) {
        super(message, t);
    }
}