<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<head>
<style>
	table, th, td {
		/* border: 1px solid gray; */
	}
	table  {
		border-collapse: collapse;
		width: 75%;
	}
	th, td {
		padding: 10px;
		
	}
	th {
		text-align: left;
		border-bottom: 1px solid black;
	}
	td {
		vertical-align: top;
		border-bottom: 1px solid #ddd;
	}
	tr:nth-child(even) {background-color: #f2f2f2};
</style>
</head>

<html>
<table id="tickets">
<tbody><tr>
  <th>Id</th>
  <th>Type</th>
  <th>Priority</th>
  <th>Name</th>
  <th>Status</th>
  <th>Created</th>
</tr>
<c:forEach items="${tickets}" var="ticket">
<tr>
<td>${ticket.id}</td>
<td><a href="/deliverable/ticket/${ticket.id}">${ticket.ticketType.name}</a></td>
<td>${ticket.priority.value}</td>
<td>${ticket.name}</td>
<td>${ticket.status.value}</td>
<td>${ticket.dateCreated}</td>
</tr>
</c:forEach>
</tbody></table>

</html>