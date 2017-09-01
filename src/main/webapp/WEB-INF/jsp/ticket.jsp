<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
Back to <a href="/deliverable/tickets">View Tickets</a>
<div id="ticket">
<div>${ticket.ticketType.name}</div>
<div> id: ${ticket.id}</div>
<div style="font-size: 32;">${ticket.name}</div>
<div id="ticketDescription" style="font-size: monospaced;">${ticket.description}</div>
<div>Priority: ${ticket.priority.value}</div>
<div>Status: ${ticket.status.value}</div>
<div>
	Date Created: <fmt:formatDate pattern="MM/dd/yyyy" value="${ticket.dateCreated}" />
</div>
</div>

<!-- todo remove
<form method="POST" action="/deliverable/tickets/${ticket.id}/name">
New name: <input type="text" name="name"/>
<input type="submit" value="Update Ticket"/>
</form>
<form method="POST" action="/deliverable/tickets/${ticket.id}/priority">
New priority: <input type="text" name="priority"/>
<input type="submit" value="Update Priority"/>
</form>
-->

</html>