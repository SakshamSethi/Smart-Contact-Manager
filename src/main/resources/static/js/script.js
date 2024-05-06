const toogleSideBar = () => {

	if ($('.sidebar').is(":visible")) {
		//true --> close krna hai
		$('.sidebar').css("display", "none");
		$('.content').css("margin-left", "0%");
	}
	else {

		//false ---> open krna hai

		$('.sidebar').css("display", "block");
		$('.content').css("margin-left", "20%");
	}

};
const search = () => {
	console.log("searching");

	let query = $('#search-input').val();
	if (query == '') {
		$(".search-result").hide();
	}
	else {
		console.log(query);

		//sending request to server

		let url = `http://localhost:8080/search/${query}`;
		fetch(url).then((response) => {
			return response.json();
		}).then((data) => {

			//console.log(data);

			let text = `<div class='list-group'>`

			data.forEach(contact => {
				text += `<a href='/user/contact/${contact.cId}' class='list-group-item list-group-item-action'> ${contact.name} </a>`
			});

			text += `</div>`
			$(".search-result").html(text);
			$(".search-result").show();
		});



	}
}


//first request- to sercer to create order

const paymentStart = () => {

	console.log("payment start");
	let amount = $("#payment_field").val();

	if (amount == '' || amount == null) {

		swal.fire({
			title: "Failed",
			text: "Amount cannot be empty!",
			icon: "error",
		});
		return;
	}

	// we will use ajax to send request to server to create order 
	// jquery ajax function !

	$.ajax(

		{
			url: '/user/create_order',
			data: JSON.stringify({ amount: amount, info: 'order_request' }),
			contentType: 'application/json',
			type: 'POST',
			dataType: 'json',

			success: function(response) {
				// this will be invoked when success
				console.log(response)
				if (response.status == "created") {
					//initiate form 
					//open payment form 

					//use generated order id

					let options = {

						key: 'rzp_test_HSiSOAFjV7KT46',
						amount: response.amount,
						currency: 'INR',
						name: 'Smart Contact Manager',
						description: 'donation',
						order_id: response.id,

						handler: function(response) {
							console.log(response.razorpay_payment_id)
							console.log(response.razorpay_order_id)
							console.log(response.razorpay_signature)
							console.log("Payment Successfull");

							updatePaymentOnServer(response.razorpay_payment_id, response.razorpay_order_id, 'paid');

							swal.fire({
								title: "Good Job",
								text: "Congrats! Payment successful!",
								icon: "success",
							});
						},
						"prefill": {
							"name": "",
							"email": "",
							"contact": ""
						},
						"notes": {
							"address": "Smart Contact Manager !"
						},
						"theme": {
							"color": "#3399cc"
						}
					};

					let rzp = new Razorpay(options);

					rzp.on('payment.failed', function(response) {
						console.log(response.error.code)
						console.log(response.error.description)
						console.log(response.error.source)
						console.log(response.error.step)
						console.log(response.error.reason)
						console.log(response.error.metadata.order_id)
						console.log(response.error.metadata.payment_id)

						swal.fire({
							title: "Oops",
							text: " Payment Failed!",
							icon: "error",
						});
					}
					);

					rzp.open()

				}

			},
			error: function(error) {
				consolr.log(error);
				alert("Something went wrong");
			}

		}
	)

};

function updatePaymentOnServer(payment_id, order_id, status)
{
	$.ajax({
		url: '/user/update_order',
		data: JSON.stringify({ payment_id: payment_id, order_id: order_id, status: status }),
		contentType: 'application/json',
		type: 'POST',
		dataType: 'json',
		success:function(response)
		{
			swal.fire({
								title: "Good Job",
								text: "Congrats! Payment successful!",
								icon: "success",
							});
		},
		error:function(response)
		{
			swal.fire({
							title: "Oops",
							text: " Payment is successfull but we didnot get it on server, we will contact as soon as possible!",
							icon: "error",
						});
		}	
	});
}
