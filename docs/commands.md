# Commands

## Public

- `/ping`
  - Returns current gateway ping.

- `/links`
  - Sends Arcane Arts docs/support links.

- `/logguide`
  - Sends support guidelines for sharing logs/code.

- `/notify`
  - Opens an interactive selector for notification topics.

- `/ticket create`
  - Creates a private support ticket (one open ticket per user).

- `/ticket list`
  - Lists open tickets (support/admin).

- `/ticket claim [ticket_id]`
  - Claims an open ticket (support/admin). If no `ticket_id` is provided, it uses the current ticket channel.

- `/ticket panel`
  - Refreshes the ticket hub panel and button controls (admin).

- `/ticket close [ticket_id]`
  - Closes the current ticket channel or a specific ticket.

## Staff/Admin

- `/setup provision`
  - Idempotently creates/repairs managed resources.

- `/setup audit`
  - Reports resource drift without mutation.

- `/ticket setup`
  - Provisions ticket resources and posts ticket panel.

- `/ticket transcript <ticket_id>`
  - Downloads transcript for a closed ticket.
