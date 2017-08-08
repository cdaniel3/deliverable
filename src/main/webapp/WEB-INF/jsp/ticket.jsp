<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<div id="ticket">
<div>${ticket.ticketType.name}</div>
<div> id: ${ticket.id}</div>
<div style="font-size: 32;">${ticket.name}</div>
<div>Priority: ${ticket.priority.value}</div>
<div>Status: ${ticket.status.value}</div>
<div>Date Created: ${ticket.dateCreated}</div>
</div>
</html>