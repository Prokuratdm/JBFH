package com.par.jbfh.storage.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FileTypeTest {

    @Test
    void clubLogoShouldHaveCorrectSubdirectory() {
        assertThat(FileType.CLUB_LOGO.getSubdirectory()).isEqualTo("logos");
    }

    @Test
    void clubLogoShouldHaveCorrectMaxSize() {
        assertThat(FileType.CLUB_LOGO.getMaxSizeBytes()).isEqualTo(200 * 1024);
    }

    @Test
    void clubLogoShouldAllowCorrectContentTypes() {
        assertThat(FileType.CLUB_LOGO.getAllowedContentTypes())
                .containsExactlyInAnyOrder("image/jpeg", "image/png", "image/webp", "image/svg+xml", "image/gif");
    }

    @Test
    void userAvatarShouldHaveCorrectSubdirectory() {
        assertThat(FileType.USER_AVATAR.getSubdirectory()).isEqualTo("avatars");
    }

    @Test
    void userAvatarShouldHaveCorrectMaxSize() {
        assertThat(FileType.USER_AVATAR.getMaxSizeBytes()).isEqualTo(1024 * 1024);
    }

    @Test
    void userAvatarShouldAllowCorrectContentTypes() {
        assertThat(FileType.USER_AVATAR.getAllowedContentTypes())
                .containsExactlyInAnyOrder("image/jpeg", "image/png", "image/webp");
    }

    @Test
    void teamLogoShouldHaveCorrectSubdirectory() {
        assertThat(FileType.TEAM_LOGO.getSubdirectory()).isEqualTo("logos/teams");
    }

    @Test
    void teamLogoShouldHaveCorrectMaxSize() {
        assertThat(FileType.TEAM_LOGO.getMaxSizeBytes()).isEqualTo(200 * 1024);
    }

    @Test
    void teamLogoShouldAllowCorrectContentTypes() {
        assertThat(FileType.TEAM_LOGO.getAllowedContentTypes())
                .containsExactlyInAnyOrder("image/jpeg", "image/png", "image/webp", "image/svg+xml", "image/gif");
    }

    @Test
    void exercisePictureShouldHaveCorrectSubdirectory() {
        assertThat(FileType.EXERCISE_PICTURE.getSubdirectory()).isEqualTo("exercises");
    }

    @Test
    void exercisePictureShouldHaveCorrectMaxSize() {
        assertThat(FileType.EXERCISE_PICTURE.getMaxSizeBytes()).isEqualTo(500 * 1024);
    }

    @Test
    void exercisePictureShouldAllowCorrectContentTypes() {
        assertThat(FileType.EXERCISE_PICTURE.getAllowedContentTypes())
                .containsExactlyInAnyOrder("image/jpeg", "image/png", "image/webp");
    }
}
