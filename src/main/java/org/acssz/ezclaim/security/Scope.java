package org.acssz.ezclaim.security;

public enum Scope {
    AUDIT,
    CLAIM_READ,
    CLAIM_WRITE,
    TAG_READ,
    TAG_WRITE,
    PHOTO_READ,
    PHOTO_WRITE,
    PHOTO_DELETE;

    public String authority() { return "SCOPE_" + name(); }
}

