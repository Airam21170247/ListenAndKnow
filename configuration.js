//STARTS CONFIGURATION
    

    import { initializeApp } from "https://www.gstatic.com/firebasejs/12.2.1/firebase-app.js";
    import { getAnalytics } from "https://www.gstatic.com/firebasejs/12.2.1/firebase-analytics.js";
    import { 
      getFirestore, 
      collection, 
      addDoc, 
      getDocs, 
      deleteDoc, 
      doc 
    } from "https://www.gstatic.com/firebasejs/12.2.1/firebase-firestore.js";

  // TODO: Add SDKs for Firebase products that you want to use
  // https://firebase.google.com/docs/web/setup#available-libraries

  // Your web app's Firebase configuration
  // For Firebase JS SDK v7.20.0 and later, measurementId is optional
 const firebaseConfig = {
  apiKey: "AIzaSyBF0N0_pujG66HGSVEJ6hwNrmVYugko1VE",
  authDomain: "listen-knowaav.firebaseapp.com",
  databaseURL: "https://listen-knowaav-default-rtdb.firebaseio.com",
  projectId: "listen-knowaav",
  storageBucket: "listen-knowaav.firebasestorage.app",
  messagingSenderId: "460120508748",
  appId: "1:460120508748:web:74f3cad3bcc9ac67e427e9",
  measurementId: "G-GYLL4MWHLC"
};

  // Initialize Firebase
  const app = initializeApp(firebaseConfig);
  const analytics = getAnalytics(app);
  const database = getFirestore(app);




  //STARTS DATABASE.

    const lista = document.getElementById("lista");

    // 🔹 Cargar datos
    async function cargar() {
      lista.innerHTML = "";
      const querySnapshot = await getDocs(collection(database, "numeros"));
      querySnapshot.forEach(docu => {
        const data = docu.data();
        lista.innerHTML += `<article>${data.Susja}</article>`;
      });
    }

    // 🔹 Agregar persona
    async function add() {
      let Susja = document.getElementById("Susja").value;
      await addDoc(collection(database, "numeros"), { Susja });
      cargar();
    }

    // 🔹 Borrar persona
    async function borrar(id) {
      await deleteDoc(doc(database, "numeros", id));
      cargar();
    }

    // Hacer funciones accesibles desde el HTML
    window.agregar = agregar;
    window.borrar = borrar;

    // Cargar al inicio
    cargar();