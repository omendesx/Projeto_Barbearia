const clientForm = document.querySelector("#clientForm");
const message = document.querySelector("#message");
const formservice = document.querySelector("#formservice");

formservice.addEventListener("submit", async (event) => {
  event.preventDefault();
  const service = {
    name: document.querySelector("#nameService").value.trim(),
    description: document.querySelector("#description").value.trim(),
    price: document.querySelector("#price").value,
    durationMinutes: Number(document.querySelector("#duration_minutes").value),
    active: true,
  };

  try {
    const response = await fetch("/api/services", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(service)
    });
    if (!response.ok) {
      const errorData = await response.json().catch(() => null);

      throw new Error(
        errorData?.message || `Erro ao cadastrar: ${response.status}`
      );
    }
  } catch (error) {
    console.log(error);
  }


});

//cadastrar cliente no banco
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

const btnClient = document.querySelector("#btnBuscar");
btnClient.addEventListener("click", () => {
  // console.log("clicou")
  BuscaCliente();

})
const btnService = document.querySelector("#btnService");
btnService.addEventListener("click", () => {
  // console.log("clicou")
  ShowServices();

})

async function ShowServices(){

  const response = await fetch("/api/services");
  const data = await response.json();
  // console.log(data);
  const showServices = document.querySelector("#showServices");


  await data.forEach((element, i) => {
    console.log(`Nome: ${element.name} | Preço: ${element.price} | Tempo: ${element.durationMinutes} | Descr. : ${element.description}`)
    showServices.innerHTML += ` <div>
     <p>Nome: ${element.name} | Preço: ${element.price} | Tempo: ${element.durationMinutes} | Descr. : ${element.description} </p>
     </div>   `;
  });

}


async function BuscaCliente() {
  const response = await fetch("/api/clients");
  const data = await response.json();
  //console.log(data[0].name);
  const showClient = document.querySelector("#showClient");


  await data.forEach((element, i) => {

    showClient.innerHTML += ` <div>
     <p>Nome: ${element.name} | Email: ${element.email} | Idade: ${element.age}</p>
     </div>   `;
  });


}

