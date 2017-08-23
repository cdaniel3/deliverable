<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
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
<form method="POST" action="/deliverable/tickets/${ticket.id}/name">
Ticket Name: <input type="text" name="name"/><br/>
<input type="submit" value="Update Ticket"/>
</form>
</html>