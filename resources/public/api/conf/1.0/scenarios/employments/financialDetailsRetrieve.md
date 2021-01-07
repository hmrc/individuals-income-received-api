<p>Scenario simulations using Gov-Test-Scenario headers is only available in the sandbox environment.</p>
<table>
    <thead>
        <tr>
            <th>Header Value (Gov-Test-Scenario)</th>
            <th>Scenario</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><p>DEFAULT_RESPONSE<br><strong>?source=user</strong></p></td>
            <td><p>Requesting <strong>user</strong> source simulates success response with User provided financial details.</p></td>
        </tr>
        <tr>
            <td><p>DEFAULT_RESPONSE<br><strong>?source=hmrcHeld</strong></p></td>
            <td><p>Requesting <strong>hmrcHeld</strong> source simulates success response with HMRC held financial details.</p></td>
        </tr>
        <tr>
            <td><p>DEFAULT_RESPONSE<br><strong>?source=latest</strong></p></td>
            <td><p>Requesting <strong>latest</strong> source simulates success response with Latest financial details, which is a combination of the HMRC held and User provided values.</p></td>
        </tr>
        <tr>
            <td><p>NOT_FOUND</p></td>
            <td><p>Simulates the scenario where no data is found.</p></td>
        </tr>
    </tbody>
</table>