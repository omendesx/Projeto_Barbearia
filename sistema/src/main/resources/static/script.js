const clientForm = document.querySelector("#clientForm");
const message = document.querySelector("#message");

clientForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const client = {
        name: document.querySelector("#name").value.trim(),
        email: document.querySelector("#email").value.trim(),
        age: Number(document.querySelector("#age").value)
    };

    try {
        const response = await fetch("/api/clients", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(client)
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => null);

            throw new Error(
                errorData?.message || `Erro ao cadastrar: ${response.status}`
            );
        }

        const savedClient = await response.json();

        message.textContent = `Cliente ${savedClient.name} cadastrado com sucesso!`;
        message.className = "success";

        clientForm.reset();

        console.log("Cliente salvo:", savedClient);
    } catch (error) {
        console.error(error);
        message.textContent = error.message || "Não foi possível cadastrar o cliente.";
        message.className = "error";
    }
});
