Feature: Resource Management
  As a client application
  I want to upload and retrieve MP3 resources
  So that audio content is persisted and available for processing

  Scenario: Successfully upload a valid MP3 file
    When I upload a valid MP3 file
    Then the response status is 200
    And the response contains a positive resource ID
    And a message is published to "resource.queue" containing that resource ID

  Scenario: Return 404 for a missing resource
    When I request GET /resources/99999
    Then the response status is 404
    And the response body contains the error message "not found"
