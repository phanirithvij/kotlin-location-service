package com.mapbox.navigation.ui.voice;

import com.mapbox.api.directions.v5.models.VoiceInstructions;
import com.mapbox.navigation.ui.NavigationView;

/**
 * Defines a contract for speech players
 * used in {@link NavigationView}.
 */
public interface SpeechPlayer {

  /**
   * Will play the given string speechAnnouncement.  If a com.mapbox.navigation.ui.voice speechAnnouncement is already playing or
   * other speechAnnouncement are already queued, the given speechAnnouncement will be queued to play after.
   *
   * @param speechAnnouncement with com.mapbox.navigation.ui.voice speechAnnouncement data.
   */
  void play(VoiceInstructions speechAnnouncement);

  /**
   * @return true if currently muted, false if not
   */
  boolean isMuted();

  /**
   * Will determine if com.mapbox.navigation.ui.voice announcements will be played or not.
   * <p>
   * If called while an announcement is currently playing, the announcement should end immediately and any
   * announcements queued should be cleared.
   *
   * @param isMuted true if should be muted, false if should not
   */
  void setMuted(boolean isMuted);

  /**
   * Used in off-route scenarios to stop current
   * announcement (if playing) and com.mapbox.navigation.ui.voice a rerouting cue.
   */
  void onOffRoute();

  /**
   * Used to stop and release the media (if needed).
   */
  void onDestroy();
}
