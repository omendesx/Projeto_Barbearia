// Localiza no DOM os elementos que o JavaScript precisará manipular.
const clientForm = document.querySelector("#clientForm");
const message = document.querySelector("#message");
const formservice = document.querySelector("#formservice");
const formPro = document.querySelector("#formPro");





formPro.addEventListener("submit", async (event) => {
  event.preventDefault();
  const professional = {
    name: document.querySelector("#namePro").value,
    email: document.querySelector("#emailPro").value,
    phone: document.querySelector("#phonePro").value.trim(),
    active: true,
    serviceIds: [Number(document.querySelector("#servicesPro").value)],

  }
  try {
    console.log(professional)
    const response = fetch("/api/professionals", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      // O corpo HTTP precisa ser texto JSON, não um objeto JavaScript.
      body: JSON.stringify(professional)
    });
    if (!response.ok) {
      const errorData = await response.json().catch(() => null);

      throw new Error(
        errorData?.message || `Erro ao cadastrar: ${response.status}`
      );
    }
  } catch (error) {
    console.log(error)
  }


})

// Escuta o envio do formulário de serviços; async permite aguardar a resposta da API.
formservice.addEventListener("submit", async (event) => {
  // Impede o comportamento padrão do formulário, que recarregaria a página.
  event.preventDefault();
  // Monta um objeto com os valores atuais dos campos.
  const service = {
    name: document.querySelector("#nameService").value.trim(),
    description: document.querySelector("#description").value.trim(),
    price: document.querySelector("#price").value,
    durationMinutes: Number(document.querySelector("#duration_minutes").value),
    active: true,
  };

  // try/catch trata tanto erros de rede quanto erros lançados abaixo.
  try {
    // fetch envia uma requisição HTTP POST para o backend Spring.
    const response = await fetch("/api/services", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      // O corpo HTTP precisa ser texto JSON, não um objeto JavaScript.
      body: JSON.stringify(service)
    });
    // fetch não lança erro automaticamente para respostas 4xx ou 5xx.
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

// Cadastra um cliente no banco por meio da API.
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

    // Converte o JSON devolvido pela API em um objeto JavaScript.
    const savedClient = await response.json();

    message.textContent = `Cliente ${savedClient.name} cadastrado com sucesso!`;
    message.className = "success";

    // Limpa os campos somente depois de uma gravação bem-sucedida.
    clientForm.reset();

    console.log("Cliente salvo:", savedClient);
  } catch (error) {
    console.error(error);
    message.textContent = error.message || "Não foi possível cadastrar o cliente.";
    message.className = "error";
  }
});

const btnPro = document.querySelector("#btnPro");
btnPro.addEventListener("click", async () => {
  const showProfessionals = document.querySelector("#showProfessionals");
  const response = await fetch("/api/professionals");
  const data = await response.json();
  console.log(data)

  data.forEach(element => {
    const services = element.services
      ?.map(service => service.name)
      .join(", ") || "Nenhum serviço cadastrado";

    showProfessionals.innerHTML = "";
    showProfessionals.innerHTML += `<div>
      <p>Nome:${element.name} </p><br />
      <p>Email:${element.email} </p><br />
      <p>Telefone:${element.phone} </p><br />
      <p>Serviços:${services} </p><br />



    </div>`;

  });


});



// Liga cada botão à função que consulta e exibe sua lista.
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

async function ShowServices() {
  // GET é o método padrão do fetch, por isso não precisa ser informado.
  const response = await fetch("/api/services");
  // Aguarda a leitura e conversão do corpo da resposta.
  const data = await response.json();
  // console.log(data);
  const showServices = document.querySelector("#showServices");


  // Percorre os serviços; forEach é síncrono, portanto o await não é necessário aqui.
  await data.forEach((element, i) => {
    console.log(`Nome: ${element.name} | Preço: ${element.price} | Tempo: ${element.durationMinutes} | Descr. : ${element.description}`)
    showServices.innerHTML += ` <div>
     <p>Nome: ${element.name} | Preço: ${element.price} | Tempo: ${element.durationMinutes} | Descr. : ${element.description} </p>
     </div>   `;
  });

}


async function BuscaCliente() {
  // Consulta todos os clientes cadastrados.
  const response = await fetch("/api/clients");
  const data = await response.json();
  //console.log(data[0].name);
  const showClient = document.querySelector("#showClient");


  // Acrescenta um bloco HTML para cada cliente recebido.
  await data.forEach((element, i) => {

    showClient.innerHTML += ` <div>
     <p>Nome: ${element.name} | Email: ${element.email} | Idade: ${element.age}</p>
     </div>   `;
  });


}

