@story:folders-001 @epic:todo-organization @priority:high @ui
Feature: Folder-based task organization

  Background:
    Given the default folder "General" exists
    And folder "Work" exists with 1 task
    And folder "Groceries" exists with 2 tasks

  Scenario: Active folder is highlighted with count
    When I open the app
    Then "General" is the active folder
    And each folder shows its task count
    And the active folder is starred

  Scenario: Selecting a folder scopes the list and input
    When I select folder "Work"
    Then the task list shows only "Work" tasks
    And the add-task placeholder reads 'Add new task in Work ...'

  Scenario: Add a task to the active folder
    Given folder "Work" is active
    When I enter "Ship release" and confirm add
    Then "Ship release" appears in the "Work" task list
    And the "Work" count increases by 1

  Scenario: Toggle task completion
    Given a task "Buy milk" exists and is not done
    When I toggle its checkbox
    Then "Buy milk" is marked done

  Scenario: Delete a task
    Given a task "Walk dog" exists in the active folder
    When I delete "Walk dog"
    Then it no longer appears
    And the active folder count decreases by 1

  Scenario: Filter by completion state
    Given the active folder has done and not-done tasks
    When I select the "Done" filter
    Then only done tasks are shown
    When I select the "Todo" filter
    Then only not-done tasks are shown

  Scenario: Delete a folder reassigns tasks to General
    Given folder "Work" has 1 task and is active
    When I choose to delete folder "Work"
    Then I see "Delete folder \"Work\"? Tasks move to General."
    When I confirm
    Then folder "Work" no longer exists
    And its task now belongs to "General"
    And "General" is the active folder
    And the "General" count increased by 1

  Scenario: Cancel folder deletion
    When I choose to delete folder "Work"
    And I cancel
    Then folder "Work" still exists with its tasks unchanged

  Scenario: General cannot be deleted
    When "General" is the active folder
    Then the delete-folder action is unavailable for "General"

  Scenario: Create a new folder
    When I create a folder named "Reading"
    Then "Reading" appears in the sidebar with count 0
    And "Reading" becomes the active folder

  Scenario: Reject invalid folder names
    When I try to create a folder with an empty name
    Then creation is rejected with an inline validation message
    When I try to create a folder named "work"
    Then creation is rejected as a duplicate

  Scenario: Theme toggle persists
    When I switch to dark theme
    And I reload the app
    Then dark theme is still applied

  Scenario: View all folders
    When I select "All folders"
    Then tasks from every folder are shown, labeled by folder
    And the delete-folder action is hidden
