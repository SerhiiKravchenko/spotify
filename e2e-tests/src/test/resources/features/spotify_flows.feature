Feature: Spotify Platform API
  As an external client
  I want to manage audio resources through the gateway
  So that I can build a music streaming experience

  Background:
    Given the full platform is running and healthy

  Scenario: Upload a valid MP3 file via POST /resources
    When I upload a valid MP3 file via POST /resources
    Then the response status is 200
    And the response contains a resource ID

  Scenario: Request a resource that does not exist
    When I request GET /resources/99999
    Then the response status is 404
    And the response body contains an error message
