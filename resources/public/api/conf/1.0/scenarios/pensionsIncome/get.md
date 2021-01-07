<p>This endpoint is now stateful, meaning it will store or return data submitted with related endpoints. <br><br>Stateless scenarios can still be simulated using Gov-Test-Scenario headers, which are only available in the sandbox environment.</p>
<table>
    <thead>
        <tr>
            <th>Header Value (Gov-Test-Scenario)</th>
            <th>Scenario</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><p>DEFAULT_PENSIONS_INCOME_GET</p></td>
            <td><p>Simulates success response.</p></td>
        </tr>
        <tr>
            <td><p>NOT_FOUND</p></td>
            <td><p>Simulates the scenario where no data is found.</p></td>
        </tr>
    </tbody>
</table>