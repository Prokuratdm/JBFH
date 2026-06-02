package com.par.jbfh.storage.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileType {

    CLUB_LOGO("logos", 200 * 1024, new String[]{"image/jpeg", "image/png", "image/webp", "image/svg+xml", "image/gif"}),
    USER_AVATAR("avatars", 1024 * 1024, new String[]{"image/jpeg", "image/png", "image/webp"}),
    TEAM_LOGO("logos/teams", 200 * 1024, new String[]{"image/jpeg", "image/png", "image/webp", "image/svg+xml", "image/gif"}),
    EXERCISE_PICTURE("exercises", 500 * 1024, new String[]{"image/jpeg", "image/png", "image/webp"});

    private final String subdirectory;
    private final long maxSizeBytes;
    private final String[] allowedContentTypes;

    public String getSubdirectory() {
        return subdirectory;
    }

    public long getMaxSizeBytes() {
        return maxSizeBytes;
    }

    public String[] getAllowedContentTypes() {
        return allowedContentTypes;
    }
}